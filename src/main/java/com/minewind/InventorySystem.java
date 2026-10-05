package com.minewind;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom movement system that completely overwrites vanilla Minecraft movement
 * Implements Morrowind-style movement mechanics:
 * - No jump height restrictions
 * - Custom walking/running speeds
 * - Sneak mechanics
 * - Swimming/flying mechanics
 * - Custom physics
 */
public class MovementSystem {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MovementSystem.class);
    
    // Movement constants
    private static final float WALK_SPEED = 4.0f;
    private static final float RUN_SPEED = 8.0f;
    private static final float SNEAK_SPEED = 2.0f;
    private static final float SWIM_SPEED = 3.0f;
    private static final float FLY_SPEED = 12.0f;
    private static final float JUMP_FORCE = 1.2f;
    private static final float GRAVITY = 0.08f;
    private static final float AIR_RESISTANCE = 0.98f;
    private static final float WATER_RESISTANCE = 0.8f;
    
    // Movement state
    private boolean isRunning;
    private boolean isSneaking;
    private boolean isFlying;
    private boolean isSwimming;
    private boolean canJump;
    
    // Velocity tracking
    private Vec3d velocity;
    private Vec3d lastPosition;
    
    public MovementSystem() {
        this.isRunning = false;
        this.isSneaking = false;
        this.isFlying = false;
        this.isSwimming = false;
        this.canJump = true;
        this.velocity = Vec3d.ZERO;
        this.lastPosition = Vec3d.ZERO;
    }
    
    public void initialize() {
        LOGGER.info("Initializing custom movement system...");
        
        // Register movement handlers
        registerMovementHandlers();
        
        LOGGER.info("Custom movement system initialized");
    }
    
    public void onClientStart() {
        LOGGER.info("Movement system started");
    }
    
    public void onClientStop() {
        LOGGER.info("Movement system stopped");
    }
    
    private void registerMovementHandlers() {
        // Handlers will be registered through mixins
        LOGGER.debug("Movement handlers registered");
    }
    
    /**
     * Update movement state based on player input
     */
    public void updateMovementState(ClientPlayerEntity player) {
        this.isRunning = player.isSprinting();
        this.isSneaking = player.isSneaking();
        this.isFlying = player.getAbilities().flying;
        this.isSwimming = player.isSwimming();
    }
    
    /**
     * Calculate movement velocity based on input and state
     */
    public Vec3d calculateMovementVelocity(ClientPlayerEntity player, Vec3d inputDirection) {
        float speed = getCurrentSpeed(player);
        
        // Calculate base velocity
        Vec3d velocity = inputDirection.multiply(speed);
        
        // Apply environment modifiers
        if (isSwimming) {
            velocity = velocity.multiply(SWIM_SPEED / speed);
        }
        
        if (isFlying) {
            velocity = velocity.multiply(FLY_SPEED / speed);
        }
        
        // Apply gravity if not flying or swimming
        if (!isFlying && !isSwimming) {
            velocity = velocity.add(0, -GRAVITY, 0);
        }
        
        // Apply resistance
        if (!isFlying) {
            velocity = velocity.multiply(AIR_RESISTANCE);
        }
        
        if (isSwimming) {
            velocity = velocity.multiply(WATER_RESISTANCE);
        }
        
        this.velocity = velocity;
        return velocity;
    }
    
    /**
     * Get current movement speed based on state
     */
    private float getCurrentSpeed(ClientPlayerEntity player) {
        if (isSneaking) {
            return SNEAK_SPEED;
        }
        
        if (isRunning) {
            return RUN_SPEED;
        }
        
        return WALK_SPEED;
    }
    
    /**
     * Handle jump action
     */
    public void handleJump(ClientPlayerEntity player) {
        if (canJump && !isSwimming && !isFlying) {
            Vec3d jumpVelocity = new Vec3d(0, JUMP_FORCE, 0);
            this.velocity = velocity.add(jumpVelocity);
            canJump = false;
            LOGGER.debug("Player jumped with force: {}", JUMP_FORCE);
        }
    }
    
    /**
     * Reset jump state when player lands
     */
    public void onLand() {
        canJump = true;
    }
    
    /**
     * Update velocity based on current movement
     */
    public void updateVelocity(Vec3d newVelocity) {
        this.velocity = newVelocity;
    }
    
    /**
     * Get current velocity
     */
    public Vec3d getVelocity() {
        return velocity;
    }
    
    /**
     * Set running state
     */
    public void setRunning(boolean running) {
        this.isRunning = running;
    }
    
    /**
     * Set sneaking state
     */
    public void setSneaking(boolean sneaking) {
        this.isSneaking = sneaking;
    }
    
    public boolean isSneaking() {
        return isSneaking;
    }
    
    /**
     * Set flying state
     */
    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }
    
    /**
     * Set swimming state
     */
    public void setSwimming(boolean swimming) {
        this.isSwimming = swimming;
    }
    
    /**
     * Check if player can jump
     */
    public boolean canJump() {
        return canJump;
    }
    
    /**
     * Apply custom physics to player
     */
    public void applyPhysics(ClientPlayerEntity player) {
        // Apply velocity to player
        if (velocity.lengthSquared() > 0) {
            player.setVelocity(velocity);
        }
    }
    
    /**
     * Get movement multiplier based on attribute (Agility)
     */
    public float getMovementMultiplier() {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            AttributeSystem attributeSystem = mod.getMorrowindSystems().getAttributeSystem();
            int agility = attributeSystem.getAttribute(MorrowindSystems.Attribute.AGILITY);
            return 1.0f + (agility / 100.0f); // Agility increases movement speed
        }
        return 1.0f;
    }
}
