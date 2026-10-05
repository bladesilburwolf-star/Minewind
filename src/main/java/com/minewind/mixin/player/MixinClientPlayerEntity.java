package com.minewind.mixin.player;

import com.minewind.MinewindMod;
import com.minewind.MovementSystem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for ClientPlayerEntity to override movement and combat
 */
@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            MovementSystem movementSystem = mod.getMovementSystem();
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
            
            // Update movement state
            movementSystem.updateMovementState(player);
            
            // Apply custom physics
            movementSystem.applyPhysics(player);
            
            // Update combat system
            mod.getCombatSystem().tick(player);
            
            // Update spell system
            mod.getSpellSystem().tick(player);
            
            // Update Morrowind systems
            mod.getMorrowindSystems().onTick(MinecraftClient.getInstance());
        }
    }
    
    @Inject(method = "jump", at = @At("HEAD"))
    private void onJump(CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            MovementSystem movementSystem = mod.getMovementSystem();
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
            
            // Handle custom jump
            movementSystem.handleJump(player);
        }
    }
    
    @Inject(method = "getMovementSpeed", at = @At("HEAD"), cancellable = true)
    private void onGetMovementSpeed(CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            MovementSystem movementSystem = mod.getMovementSystem();
            float multiplier = movementSystem.getMovementMultiplier();
            
            // Apply custom movement speed
            float baseSpeed = 0.1f; // Default walking speed
            ci.setReturnValue(baseSpeed * multiplier);
        }
    }
}
