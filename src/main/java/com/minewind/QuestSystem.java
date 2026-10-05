package com.minewind;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class MovementSystem {
    private boolean running;
    private boolean sneaking;
    private boolean flying;
    private boolean swimming;

    public void initialize() {
        running = false;
        sneaking = false;
        flying = false;
        swimming = false;
    }

    public void updateMovementState(ClientPlayerEntity player) {
        if (player == null) {
            return;
        }
        running = player.isSprinting();
        sneaking = player.isSneaking();
        flying = player.getAbilities().flying;
        swimming = player.isSwimming();
    }

    public Vec3d calculateMovementVelocity(ClientPlayerEntity player, Vec3d input) {
        if (player == null || input == null) {
            return Vec3d.ZERO;
        }
        float speed = running ? 0.14f : 0.09f;
        if (sneaking) {
            speed *= 0.6f;
        }
        if (flying) {
            speed *= 1.8f;
        }
        if (swimming) {
            speed *= 0.75f;
        }
        return new Vec3d(input.x * speed, input.y * speed, input.z * speed);
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isFlying() {
        return flying;
    }

    public boolean isSwimming() {
        return swimming;
    }
}
