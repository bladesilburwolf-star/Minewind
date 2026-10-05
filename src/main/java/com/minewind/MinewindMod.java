package com.minewind.mixin.render;

import com.minewind.MinewindMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getFov", at = @At("HEAD"), cancellable = true)
    private void minewind$modifyFov(Camera camera, float tickDelta, boolean changingFov,
                                     CallbackInfoReturnable<Double> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        double fov = 70.0;
        if (mod.getMovementSystem().isSneaking()) {
            fov -= 10.0;
        }
        if (mod.getSpellSystem().isCasting()) {
            fov -= mod.getSpellSystem().getCastProgress() * 20.0;
        }
        cir.setReturnValue(fov);
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void minewind$disableVanillaBob(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (MinewindMod.getInstance() != null) {
            ci.cancel();
        }
    }
}
