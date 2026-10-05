package com.minewind.mixin.player;

import com.minewind.MinewindMod;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for PlayerEntity to override health, magicka, and other attribute-related systems.
 * This decouples the player from vanilla health mechanics.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    /**
     * Override getMaxHealth to use Morrowind's attribute-based health system
     */
    @Inject(method = "getMaxHealth", at = @At("HEAD"), cancellable = true)
    private void onGetMaxHealth(CallbackInfoReturnable<Float> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        float health = mod.getMorrowindSystems().getAttributeSystem().getHealth();
        cir.setReturnValue(health);
    }

    /**
     * Override getHealth to use Morrowind's attribute-based health system
     */
    @Inject(method = "getHealth", at = @At("HEAD"), cancellable = true)
    private void onGetHealth(CallbackInfoReturnable<Float> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        float health = mod.getMorrowindSystems().getAttributeSystem().getHealth();
        cir.setReturnValue(health);
    }

    /**
     * Override setHealth to prevent vanilla from modifying health directly
     */
    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void onSetHealth(float health, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // In Morrowind, health is managed by the AttributeSystem
        // We'll handle health changes through our own systems
        // For now, just prevent vanilla from setting health directly
        
        // If this is a damage/death scenario, we might want to handle it differently
        // But for now, we'll just cancel and let our systems handle health
        cir.cancel();
    }

    /**
     * Override damage to use custom combat system
     */
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(CallbackInfoReturnable<Boolean> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // Custom damage handling will be implemented in CombatSystem
        // For now, just log and allow damage
        // TODO: Implement custom damage calculation based on skills/attributes
    }

    /**
     * Override heal to use custom health system
     */
    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void onHeal(float amount, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // Health regeneration is handled by our AttributeSystem
        // Restoration spells will handle healing
        cir.cancel();
    }

    /**
     * Override canBreatheInWater to allow underwater exploration without drowning
     * Morrowind doesn't have a breathing mechanic
     */
    @Inject(method = "canBreatheInWater", at = @At("HEAD"), cancellable = true)
    private void onCanBreatheInWater(CallbackInfoReturnable<Boolean> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // In Morrowind, players can swim/breathe underwater indefinitely
        cir.setReturnValue(true);
    }

    /**
     * Override getAir to prevent drowning
     */
    @Inject(method = "getAir", at = @At("HEAD"), cancellable = true)
    private void onGetAir(CallbackInfoReturnable<Integer> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // Always return max air to prevent drowning
        cir.setReturnValue(300); // Default max air
    }

    /**
     * Override setAir to prevent vanilla from modifying air
     */
    @Inject(method = "setAir", at = @At("HEAD"), cancellable = true)
    private void onSetAir(int air, CallbackInfo ci) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // Prevent vanilla from reducing air
        cir.cancel();
    }

    /**
     * Override getMovementSpeed to use custom movement calculations
     */
    @Inject(method = "getMovementSpeed", at = @At("HEAD"), cancellable = true)
    private void onGetMovementSpeed(CallbackInfoReturnable<Float> cir) {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod == null) return;

        // Movement speed is handled by MovementSystem
        // This is a fallback for any vanilla code that checks movement speed
        cir.setReturnValue(0.1f); // Base speed, will be modified by MovementSystem
    }

}
