package com.minewind.mixin.world;

import com.minewind.MinewindMod;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for ClientWorld to handle custom world updates and entity management.
 * This is used for custom entity models and world-specific behaviors.
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    /**
     * Inject at the start of tick to update custom systems
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            // Update model system for custom entity rendering
            mod.getModelSystem().onClientStart();
        }
    }

    /**
     * Inject at the end of tick to clean up or finalize custom systems
     */
    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickEnd(CallbackInfo ci) {
        // Any post-tick cleanup can go here
    }
}
