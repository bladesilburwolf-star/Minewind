package com.minewind.mixin.render;

import com.minewind.MinewindMod;
import com.minewind.HUDSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for GameRenderer to handle custom HUD rendering and viewmodel rendering.
 * This decouples the rendering pipeline from vanilla behavior.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    /**
     * Inject after world rendering to render custom HUD elements
     */
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;)V", shift = At.Shift.AFTER))
    private void onAfterWorldRender(float tickDelta, long nanoTime, boolean renderWorld, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        HUDSystem hudSystem = mod.getHudSystem();
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;

        // The HUD is rendered through HudRenderCallback, but we can add additional rendering here
        // For viewmodel rendering in first-person
        renderViewModels(tickDelta);
    }

    /**
     * Render first-person viewmodels
     */
    private void renderViewModels(float tickDelta) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options == null) return;

        // Only render viewmodels in first-person
        if (client.options.getPerspective() != 0) return; // 0 = first-person

        ModelSystem modelSystem = mod.getModelSystem();

        // Get active viewmodel (weapon, spell, etc.)
        ModelSystem.CustomModel viewModel = modelSystem.getModel("weapon/sword_view");
        
        if (viewModel != null && viewModel.isLoaded() && viewModel.isViewModel()) {
            // Render the viewmodel
            MatrixStack matrices = new MatrixStack();
            
            // Apply camera transformations
            CameraMixin cameraMixin = getCameraMixin();
            if (cameraMixin != null) {
                // Use camera's custom transformations
                matrices.multiply(cameraMixin.calculateViewModelOffset(client.player).toMatrix());
            }
            
            // Render with viewmodel animations
            // This would use the model system's rendering
            modelSystem.renderModel(viewModel, client.player, matrices, 
                client.getBufferBuilders().getEntityVertexConsumers(),
                client.world.getLightmapCoordinates(client.player.getBlockPos()),
                client.player.getId());
        }
    }

    /**
     * Helper method to get CameraMixin instance (for accessing custom transformations)
     */
    private CameraMixin getCameraMixin() {
        // This is a helper to access camera transformations
        // In practice, we'd need to track the camera instance
        return null;
    }

    /**
     * Inject to modify the projection matrix for custom FOV or other effects
     */
    @Inject(method = "getBasicProjectionMatrix", at = @At("HEAD"), cancellable = true)
    private void onGetBasicProjectionMatrix(double fov, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // Custom FOV or projection effects can be applied here
        // For now, use vanilla behavior
    }

    /**
     * Inject to modify FOV based on player state (sprinting, spells, etc.)
     */
    @Inject(method = "getFov", at = @At("HEAD"), cancellable = true)
    private void onGetFov(float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Get base FOV
        double fov = 70.0; // Default Morrowind FOV

        // Adjust FOV based on player state
        if (mod.getMovementSystem() != null) {
            if (mod.getMovementSystem().isSneaking()) {
                fov -= 10.0; // Sneaking reduces FOV
            }
        }

        // Spell effects can modify FOV
        if (mod.getSpellSystem() != null && mod.getSpellSystem().isCasting()) {
            float castProgress = mod.getSpellSystem().getCastProgress();
            fov -= castProgress * 20.0; // Casting reduces FOV
        }

        cir.setReturnValue(fov);
    }

    /**
     * Inject to handle custom view bobbing
     */
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void onBobView(MatrixStack matrices, float distance, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Custom view bobbing based on Morrowind-style movement
        // In Morrowind, view bobbing is more subtle and based on walking speed
        
        // For now, we'll disable vanilla bobbing and handle it in CameraMixin
        // This prevents double-bobbing
        ci.cancel();
    }

    /**
     * Inject to handle custom hand bobbing
     */
    @Inject(method = "bobHand", at = @At("HEAD"), cancellable = true)
    private void onBobHand(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // Custom hand bobbing for viewmodels
        // We handle this in CameraMixin for consistency
        ci.cancel();
    }
}
