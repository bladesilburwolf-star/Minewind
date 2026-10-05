package com.minewind.mixin.world;

import com.minewind.MinewindMod;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for ClientWorld to handle custom world rendering and entities
 */
@Mixin(ClientWorld.class)
public abstract class MixinClientWorld {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            // Update model system
            mod.getModelSystem().onClientStart();
        }
    }
}
