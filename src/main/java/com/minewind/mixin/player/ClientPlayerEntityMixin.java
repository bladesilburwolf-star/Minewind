package com.minewind.mixin.player;

import com.minewind.MinewindMod;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "getMaxHealth", at = @At("HEAD"), cancellable = true)
    private void onGetMaxHealth(CallbackInfoReturnable<Float> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;
        cir.setReturnValue(mod.getMorrowindSystems().getAttributeSystem().getHealth());
    }

    @Inject(method = "getHealth", at = @At("HEAD"), cancellable = true)
    private void onGetHealth(CallbackInfoReturnable<Float> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;
        cir.setReturnValue(mod.getMorrowindSystems().getAttributeSystem().getHealth());
    }

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void onSetHealth(float health, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;
        ci.cancel();
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(CallbackInfoReturnable<Boolean> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void onHeal(float amount, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;
        ci.cancel();
    }

    @Inject(method = "canBreatheInWater", at = @At("HEAD"), cancellable = true)
    private void onCanBreatheInWater(CallbackInfoReturnable<Boolean> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;
        cir.setReturnValue(true);
    }

    @Inject(method = "getAir", at = @At("HEAD"), cancellable = true)
    private void onGetAir(CallbackInfoReturnable<Integer> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;
        cir.setReturnValue(300);
    }

    @Inject(method = "setAir", at = @At("HEAD"), cancellable = true)
    private void onSetAir(int air, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;
        ci.cancel();
    }

    @Inject(method = "getMovementSpeed", at = @At("HEAD"), cancellable = true)
    private void onGetMovementSpeed(CallbackInfoReturnable<Float> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;
        cir.setReturnValue(0.1f);
    }
}
