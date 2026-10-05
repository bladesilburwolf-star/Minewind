package com.minewind.mixin.render;

import com.minewind.MinewindMod;
import com.minewind.ModelSystem;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dedicated mixin for Camera to completely decouple from vanilla camera behavior.
 * This allows custom first-person viewmodels, Morrowind-style camera positioning,
 * and custom camera effects (spells, etc.).
 */
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Unique
    private boolean minewind$cameraDecoupled = false;

    @Unique
    private float minewind$customYaw = 0.0f;

    @Unique
    private float minewind$customPitch = 0.0f;

    @Unique
    private float minewind$customRoll = 0.0f;

    @Unique
    private Vec3d minewind$customPosition = Vec3d.ZERO;

    @Unique
    private float minewind$viewModelBobbing = 0.0f;

    @Unique
    private float minewind$viewModelSwing = 0.0f;

    /**
     * Store reference to the camera instance for custom calculations
     */
    @Unique
    private Camera minewind$cameraInstance;

    /**
     * Capture camera instance at construction
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(CallbackInfo ci) {
        minewind$cameraInstance = (Camera) (Object) this;
    }

    /**
     * Inject at the start of update to capture pre-update state
     */
    @Inject(method = "update", at = @At("HEAD"))
    private void onUpdateStart(Entity focusedEntity, boolean thirdPerson, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // Mark that we're decoupling the camera
        minewind$cameraDecoupled = true;

        // Get current rotation for reference
        if (focusedEntity != null) {
            minewind$customYaw = focusedEntity.getYaw();
            minewind$customPitch = focusedEntity.getPitch();
        }
    }

    /**
     * Inject at the end of update to apply custom camera transformations
     */
    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdateEnd(Entity focusedEntity, boolean thirdPerson, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        Camera camera = minewind$cameraInstance;
        ModelSystem modelSystem = mod.getModelSystem();

        // Apply custom camera effects based on player state
        applyCustomCameraEffects(camera, focusedEntity, thirdPerson);
    }

    /**
     * Modify the eye position to support custom viewmodels
     */
    @ModifyVariable(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = At.Shift.BY, by = -1), ordinal = 0)
    private Vec3d modifyEyePosition(Vec3d original, Entity focusedEntity, boolean thirdPerson) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return original;

        ModelSystem modelSystem = mod.getModelSystem();

        // In first-person, adjust position for viewmodel rendering
        if (!thirdPerson && focusedEntity != null) {
            // Apply viewmodel offsets
            Vec3d viewModelOffset = calculateViewModelOffset(focusedEntity);
            return original.add(viewModelOffset);
        }

        return original;
    }

    /**
     * Modify yaw to support custom camera angles
     */
    @ModifyVariable(method = "update", at = @At("HEAD"), ordinal = 0)
    private float modifyYaw(float original, Entity focusedEntity, boolean thirdPerson) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return original;

        // Apply custom yaw modifications (spell effects, etc.)
        return original + minewind$customYaw;
    }

    /**
     * Modify pitch to support custom camera angles
     */
    @ModifyVariable(method = "update", at = @At("HEAD"), ordinal = 1)
    private float modifyPitch(float original, Entity focusedEntity, boolean thirdPerson) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return original;

        // Apply custom pitch modifications (spell effects, etc.)
        return original + minewind$customPitch;
    }

    /**
     * Modify roll to support custom camera effects
     */
    @ModifyVariable(method = "update", at = @At("HEAD"), ordinal = 2)
    private float modifyRoll(float original, Entity focusedEntity, boolean thirdPerson) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return original;

        // Apply custom roll (for spell effects, etc.)
        return original + minewind$customRoll;
    }

    /**
     * Apply custom camera effects (bobbing, swinging, spell effects)
     */
    @Unique
    private void applyCustomCameraEffects(Camera camera, Entity focusedEntity, boolean thirdPerson) {
        if (focusedEntity == null) return;

        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // Only apply effects in first-person
        if (thirdPerson) return;

        // Update viewmodel animations
        updateViewModelAnimations(focusedEntity);

        // Apply viewmodel bobbing (walking animation)
        applyViewModelBobbing(camera);

        // Apply viewmodel swing (attack animation)
        applyViewModelSwing(camera);
    }

    /**
     * Update viewmodel animation state based on player movement
     */
    @Unique
    private void updateViewModelAnimations(Entity focusedEntity) {
        // Get player movement state
        float forwardSpeed = 0.0f;
        float strafeSpeed = 0.0f;

        if (focusedEntity != null) {
            Vec3d velocity = focusedEntity.getVelocity();
            forwardSpeed = (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            strafeSpeed = (float) Math.abs(velocity.x);
        }

        // Update bobbing based on movement speed
        float bobbingIntensity = Math.min(forwardSpeed * 0.1f, 0.3f);
        minewind$viewModelBobbing = (float) Math.sin(System.currentTimeMillis() * 0.005f) * bobbingIntensity;

        // Update swing based on combat state
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null && mod.getCombatSystem().isAttacking()) {
            float attackProgress = mod.getCombatSystem().getAttackAnimationProgress();
            minewind$viewModelSwing = (float) Math.sin(attackProgress * Math.PI) * 0.5f;
        } else {
            minewind$viewModelSwing *= 0.9f; // Damping
        }
    }

    /**
     * Apply viewmodel bobbing effect
     */
    @Unique
    private void applyViewModelBobbing(Camera camera) {
        if (minewind$viewModelBobbing == 0.0f) return;

        // Apply vertical bobbing
        Vec3d pos = camera.getPos();
        camera.setPos(pos.x, pos.y + minewind$viewModelBobbing * 0.05f, pos.z);

        // Apply slight rotation bobbing
        minewind$customPitch += minewind$viewModelBobbing * 0.5f;
    }

    /**
     * Apply viewmodel swing effect
     */
    @Unique
    private void applyViewModelSwing(Camera camera) {
        if (minewind$viewModelSwing == 0.0f) return;

        // Apply horizontal swing
        minewind$customYaw += minewind$viewModelSwing * 2.0f;
    }

    /**
     * Calculate viewmodel offset for first-person rendering
     */
    @Unique
    private Vec3d calculateViewModelOffset(Entity focusedEntity) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return Vec3d.ZERO;

        ModelSystem modelSystem = mod.getModelSystem();

        // Get the active viewmodel
        ModelSystem.CustomModel viewModel = modelSystem.getModel("weapon/sword_view");
        
        if (viewModel != null && viewModel.isViewModel()) {
            // Custom viewmodel positioning
            // In Morrowind, weapons are rendered in first-person with specific offsets
            float forwardOffset = 0.3f;
            float rightOffset = 0.15f;
            float upOffset = -0.2f;

            // Calculate direction vectors
            float yawRad = (float) Math.toRadians(focusedEntity.getYaw());
            float pitchRad = (float) Math.toRadians(focusedEntity.getPitch());

            float sinYaw = MathHelper.sin(yawRad);
            float cosYaw = MathHelper.cos(yawRad);
            float sinPitch = MathHelper.sin(pitchRad);
            float cosPitch = MathHelper.cos(pitchRad);

            // Calculate offsets in world space
            float xOffset = -sinYaw * cosPitch * forwardOffset + cosYaw * rightOffset;
            float yOffset = -sinPitch * forwardOffset + upOffset;
            float zOffset = cosYaw * cosPitch * forwardOffset + sinYaw * rightOffset;

            return new Vec3d(xOffset, yOffset, zOffset);
        }

        // Default first-person offset (similar to vanilla but adjustable)
        return new Vec3d(0.0, 0.0, 0.0);
    }

    /**
     * Set custom camera yaw (for spell effects, etc.)
     */
    public void setCustomYaw(float yaw) {
        minewind$customYaw = yaw;
    }

    /**
     * Set custom camera pitch (for spell effects, etc.)
     */
    public void setCustomPitch(float pitch) {
        minewind$customPitch = pitch;
    }

    /**
     * Set custom camera roll (for spell effects, etc.)
     */
    public void setCustomRoll(float roll) {
        minewind$customRoll = roll;
    }

    /**
     * Reset custom camera transformations
     */
    public void resetCustomTransformations() {
        minewind$customYaw = 0.0f;
        minewind$customPitch = 0.0f;
        minewind$customRoll = 0.0f;
    }

    /**
     * Check if camera is decoupled from vanilla behavior
     */
    public boolean isCameraDecoupled() {
        return minewind$cameraDecoupled;
    }
}
