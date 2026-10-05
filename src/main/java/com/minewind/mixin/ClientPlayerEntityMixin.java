package com.minewind.mixin;

import com.minewind.MinewindMod;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "getMovementSpeed", at = @At("HEAD"), cancellable = true)
    private void minewind$overrideMovementSpeed(CallbackInfoReturnable<Float> cir) {
        if (MinewindMod.getInstance() != null) {
            cir.setReturnValue(0.1f);
        }
    }
}
