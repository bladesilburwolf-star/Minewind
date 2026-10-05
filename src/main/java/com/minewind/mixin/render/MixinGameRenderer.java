package com.minewind.mixin.render;

import com.minewind.MinewindMod;
import com.minewind.HUDSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for GameRenderer to handle custom HUD rendering
 */
@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(float tickDelta, long nanoTime, boolean renderWorld, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            HUDSystem hudSystem = mod.getHudSystem();
            // HUD rendering is handled through HudRenderCallback
        }
    }
}
