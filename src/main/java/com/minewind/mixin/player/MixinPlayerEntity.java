package com.minewind.mixin.player;

import com.minewind.MinewindMod;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for PlayerEntity to override various behaviors
 */
@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {
    
    @Inject(method = "getMaxHealth", at = @At("HEAD"), cancellable = true)
    private void onGetMaxHealth(CallbackInfoReturnable<Float> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            float health = mod.getMorrowindSystems().getAttributeSystem().getHealth();
            cir.setReturnValue(health);
        }
    }
    
    @Inject(method = "getHealth", at = @At("HEAD"), cancellable = true)
    private void onGetHealth(CallbackInfoReturnable<Float> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            float health = mod.getMorrowindSystems().getAttributeSystem().getHealth();
            cir.setReturnValue(health);
        }
    }
    
    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(CallbackInfoReturnable<Boolean> cir) {
        // Custom damage handling will be implemented here
        // For now, just log the damage
    }
}
