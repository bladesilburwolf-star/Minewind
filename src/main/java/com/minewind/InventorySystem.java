package com.minewind;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovementSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovementSystem.class);

    private static final float WALK_SPEED = 4.0f;
    private static final float RUN_SPEED = 8.0f;
    private static final float SNEAK_SPEED = 2.0f;
    private static final float SWIM_SPEED = 3.0f;
    private static final float FLY_SPEED = 12.0f;
    private static final float JUMP_FORCE = 1.2f;
    private static final float GRAVITY = 0.08f;
    private static final float AIR_RESISTANCE = 0.98f;
    private static final float WATER_RESISTANCE = 0.8f;

    private boolean isRunning;
    private boolean isSneaking;
    private boolean isFlying;
    private boolean isSwimming;
    private boolean canJump;
    private Vec3d velocity;

    public MovementSystem() {
        this.isRunning = false;
        this.isSneaking = false;
        this.isFlying = false;
        this.isSwimming = false;
        this.canJump = true;
        this.velocity = Vec3d.ZERO;
    }

    public void initialize() {
        LOGGER.info("Initializing custom movement system...");
    }

    public void onClientStart() {
        LOGGER.info("Movement system started");
    }

    public void onClientStop() {
        LOGGER.info("Movement system stopped");
    }

    public void updateMovementState(ClientPlayerEntity player) {
        this.isRunning = player.isSprinting();
        this.isSneaking = player.isSneaking();
        this.isFlying = player.getAbilities().flying;
        this.isSwimming = player.isSwimming();
    }

    public Vec3d calculateMovementVelocity(ClientPlayerEntity player, Vec3d inputDirection) {
        float speed = getCurrentSpeed(player);
        Vec3d result = inputDirection.multiply(speed);

        if (isSwimming) {
            result = result.multiply(SWIM_SPEED / speed);
        }
        if (isFlying) {
            result = result.multiply(FLY_SPEED / speed);
        }
        if (!isFlying && !isSwimming) {
            result = result.add(0, -GRAVITY, 0);
        }
        if (!isFlying) {
            result = result.multiply(AIR_RESISTANCE);
        }
        if (isSwimming) {
            result = result.multiply(WATER_RESISTANCE);
        }

        this.velocity = result;
        return result;
    }

    private float getCurrentSpeed(ClientPlayerEntity player) {
        if (isSneaking) {
            return SNEAK_SPEED;
        }
        if (isRunning) {
            return RUN_SPEED;
        }
        return WALK_SPEED;
    }

    public void handleJump(ClientPlayerEntity player) {
        if (canJump && !isSwimming && !isFlying) {
            Vec3d jumpVelocity = new Vec3d(0, JUMP_FORCE, 0);
            this.velocity = this.velocity.add(jumpVelocity);
            canJump = false;
        }
    }

    public void onLand() {
        canJump = true;
    }

    public void updateVelocity(Vec3d newVelocity) {
        this.velocity = newVelocity;
    }

    public Vec3d getVelocity() {
        return velocity;
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
    }

    public void setSneaking(boolean sneaking) {
        this.isSneaking = sneaking;
    }

    public boolean isSneaking() {
        return isSneaking;
    }

    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    public void setSwimming(boolean swimming) {
        this.isSwimming = swimming;
    }

    public boolean canJump() {
        return canJump;
    }

    public float getMovementMultiplier() {
        MinewindMod mod = MinewindMod.getInstance();
        if (mod != null) {
            AttributeSystem attributeSystem = mod.getMorrowindSystems().getAttributeSystem();
            int agility = attributeSystem.getAttribute(MorrowindSystems.Attribute.AGILITY);
            return 1.0f + (agility / 100.0f);
        }
        return 1.0f;
    }
}
