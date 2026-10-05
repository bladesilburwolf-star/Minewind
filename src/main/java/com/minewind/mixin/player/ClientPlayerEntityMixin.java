package com.minewind.mixin.player;

import com.minewind.MinewindMod;
import com.minewind.MovementSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Dedicated mixin for ClientPlayerEntity to completely decouple from vanilla movement physics.
 * This mixin overrides all ground/jump physics and replaces them with custom Morrowind-style movement.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Unique
    private boolean minewind$wasOnGround;

    @Unique
    private boolean minewind$wasJumping;

    @Unique
    private float minewind$customYaw;

    @Unique
    private float minewind$customPitch;

    @Unique
    private Vec3d minewind$customVelocity = Vec3d.ZERO;

    @Unique
    private boolean minewind$movementDecoupled = false;

    /**
     * Inject at the start of tick to capture pre-tick state
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MinewindMod mod = MinewindMod.getInstance();
        
        if (mod == null) return;

        MovementSystem movementSystem = mod.getMovementSystem();
        
        // Capture previous state
        minewind$wasOnGround = player.isOnGround();
        minewind$wasJumping = player.jumping;
        
        // Store current rotation for custom camera control
        minewind$customYaw = player.getYaw();
        minewind$customPitch = player.getPitch();
        
        // Sync movement system with player state
        movementSystem.updateMovementState(player);
        
        // Mark that we're decoupling movement
        minewind$movementDecoupled = true;
    }

    /**
     * Inject at the end of tick to apply custom movement after vanilla processing
     */
    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickEnd(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MinewindMod mod = MinewindMod.getInstance();
        
        if (mod == null) return;

        MovementSystem movementSystem = mod.getMovementSystem();
        
        // Handle landing detection
        if (minewind$wasOnGround && !player.isOnGround()) {
            // Player just left ground
        } else if (!minewind$wasOnGround && player.isOnGround()) {
            // Player just landed
            movementSystem.onLand();
        }
        
        // Apply custom physics
        Vec3d customVelocity = movementSystem.getVelocity();
        if (customVelocity.lengthSquared() > 0) {
            // Only apply custom velocity if movement system has set one
            // This allows gradual transition from vanilla to custom physics
            player.setVelocity(customVelocity);
        }
        
        // Update combat system
        mod.getCombatSystem().tick(player);
        
        // Update spell system
        mod.getSpellSystem().tick(player);
        
        // Update Morrowind systems
        mod.getMorrowindSystems().onTick(MinecraftClient.getInstance());
    }

    /**
     * Override jump method entirely to prevent vanilla jump logic
     */
    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MovementSystem movementSystem = mod.getMovementSystem();
        
        // Use custom jump logic
        movementSystem.handleJump(player);
        
        // Cancel vanilla jump processing
        ci.cancel();
    }

    /**
     * Override travel method to completely decouple from vanilla movement physics
     * This is where vanilla handles ground movement, swimming, flying, etc.
     */
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravel(Vec3d movementInput, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MovementSystem movementSystem = mod.getMovementSystem();
        
        // Calculate custom movement based on input
        Vec3d customVelocity = movementSystem.calculateMovementVelocity(player, movementInput);
        
        // Apply the custom velocity
        player.setVelocity(customVelocity);
        
        // Mark that we've applied custom movement
        minewind$customVelocity = customVelocity;
        
        // Cancel vanilla travel processing
        ci.cancel();
    }

    /**
     * Override getMovementSpeed to use custom movement calculations
     */
    @Inject(method = "getMovementSpeed", at = @At("HEAD"), cancellable = true)
    private void onGetMovementSpeed(CallbackInfoReturnable<Float> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        MovementSystem movementSystem = mod.getMovementSystem();
        float multiplier = movementSystem.getMovementMultiplier();
        
        // Base speed - this will be modified by the movement system
        float baseSpeed = 0.1f;
        
        cir.setReturnValue(baseSpeed * multiplier);
    }

    /**
     * Override isInFluid to prevent vanilla from applying fluid physics
     */
    @Inject(method = "isInFluid", at = @At("HEAD"), cancellable = true)
    private void onIsInFluid(CallbackInfoReturnable<Boolean> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // Return false to prevent vanilla fluid physics
        // Our movement system handles swimming separately
        cir.setReturnValue(false);
    }

    /**
     * Override isTouchingWater to prevent vanilla water physics
     */
    @Inject(method = "isTouchingWater", at = @At("HEAD"), cancellable = true)
    private void onIsTouchingWater(CallbackInfoReturnable<Boolean> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // Return false to prevent vanilla water physics
        cir.setReturnValue(false);
    }

    /**
     * Override isSubmergedInWater to prevent vanilla water physics
     */
    @Inject(method = "isSubmergedInWater", at = @At("HEAD"), cancellable = true)
    private void onIsSubmergedInWater(CallbackInfoReturnable<Boolean> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // Return false to prevent vanilla water physics
        cir.setReturnValue(false);
    }

    /**
     * Override getFluidHeight to prevent vanilla fluid physics
     */
    @Inject(method = "getFluidHeight", at = @At("HEAD"), cancellable = true)
    private void onGetFluidHeight(CallbackInfoReturnable<Double> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // Return 0 to prevent vanilla fluid physics
        cir.setReturnValue(0.0);
    }

}
