package com.minewind.mixin.player;

import com.minewind.MinewindMod;
import com.minewind.MovementSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Unique
    private boolean minewind$wasOnGround;

    @Unique
    private float minewind$customYaw;

    @Unique
    private float minewind$customPitch;

    @Unique
    private Vec3d minewind$customVelocity = Vec3d.ZERO;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        MovementSystem movementSystem = mod.getMovementSystem();
        minewind$wasOnGround = player.isOnGround();
        minewind$customYaw = player.getYaw();
        minewind$customPitch = player.getPitch();
        movementSystem.updateMovementState(player);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickEnd(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        MovementSystem movementSystem = mod.getMovementSystem();
        if (minewind$wasOnGround && !player.isOnGround()) {
            // no-op
        } else if (!minewind$wasOnGround && player.isOnGround()) {
            movementSystem.onLand();
        }

        Vec3d customVelocity = movementSystem.getVelocity();
        if (customVelocity.lengthSquared() > 0) {
            player.setVelocity(customVelocity);
        }

        mod.getCombatSystem().tick(player);
        mod.getSpellSystem().tick(player);
        mod.getMorrowindSystems().onTick(MinecraftClient.getInstance());
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        mod.getMovementSystem().handleJump(player);
        ci.cancel();
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravel(Vec3d movementInput, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        Vec3d customVelocity = mod.getMovementSystem().calculateMovementVelocity(player, movementInput);
        player.setVelocity(customVelocity);
        minewind$customVelocity = customVelocity;
        ci.cancel();
    }

    @Inject(method = "getMovementSpeed", at = @At("HEAD"), cancellable = true)
    private void onGetMovementSpeed(CallbackInfoReturnable<Float> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;
        float multiplier = mod.getMovementSystem().getMovementMultiplier();
        cir.setReturnValue(0.1f * multiplier);
    }

    @Inject(method = "isInFluid", at = @At("HEAD"), cancellable = true)
    private void onIsInFluid(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "isTouchingWater", at = @At("HEAD"), cancellable = true)
    private void onIsTouchingWater(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "isSubmergedInWater", at = @At("HEAD"), cancellable = true)
    private void onIsSubmergedInWater(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "getFluidHeight", at = @At("HEAD"), cancellable = true)
    private void onGetFluidHeight(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(0.0d);
    }
}
