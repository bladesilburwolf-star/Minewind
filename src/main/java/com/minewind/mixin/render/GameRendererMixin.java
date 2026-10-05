package com.minewind.mixin.render;

import com.minewind.MinewindMod;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Unique private float minewind$customYaw;
    @Unique private float minewind$customPitch;
    @Unique private float minewind$customRoll;
    @Unique private float minewind$viewModelBobbing;
    @Unique private float minewind$viewModelSwing;

    @Inject(method = "update", at = @At("TAIL"))
    private void minewind$updateCamera(BlockView area, Entity focusedEntity, boolean thirdPerson,
                                       boolean inverseView, float tickDelta, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null || focusedEntity == null || thirdPerson) return;

        updateViewModelAnimations(focusedEntity, tickDelta);
    }

    @Unique
    private void updateViewModelAnimations(Entity focusedEntity, float tickDelta) {
        double horizontalSpeed = Math.sqrt(
            focusedEntity.getVelocity().x * focusedEntity.getVelocity().x +
            focusedEntity.getVelocity().z * focusedEntity.getVelocity().z);
        float intensity = Math.min((float) horizontalSpeed * 0.1f, 0.3f);
        minewind$viewModelBobbing = (float) Math.sin((focusedEntity.age + tickDelta) * 0.35f) * intensity;

        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null && mod.getCombatSystem().isAttacking()) {
            float progress = mod.getCombatSystem().getAttackAnimationProgress();
            minewind$viewModelSwing = (float) Math.sin(progress * Math.PI) * 0.5f;
        } else {
            minewind$viewModelSwing *= 0.9f;
        }
    }

    public void setCustomYaw(float yaw) { minewind$customYaw = yaw; }
    public void setCustomPitch(float pitch) { minewind$customPitch = pitch; }
    public void setCustomRoll(float roll) { minewind$customRoll = roll; }

    public void resetCustomTransformations() {
        minewind$customYaw = 0.0f;
        minewind$customPitch = 0.0f;
        minewind$customRoll = 0.0f;
    }

    public boolean isCameraDecoupled() {
        return MinewindMod.getInstance() != null;
    }
}
