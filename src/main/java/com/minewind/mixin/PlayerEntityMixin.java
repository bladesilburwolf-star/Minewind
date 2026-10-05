package com.minewind;

import net.minecraft.client.network.ClientPlayerEntity;

public class SpellSystem {
    private boolean casting;
    private float castProgress;

    public void initialize() {
        casting = false;
        castProgress = 0.0f;
    }

    public void tick(ClientPlayerEntity player) {
        if (casting) {
            castProgress = Math.min(1.0f, castProgress + 0.08f);
            if (castProgress >= 1.0f) {
                casting = false;
                castProgress = 0.0f;
            }
        }
    }

    public void startCasting() {
        casting = true;
        castProgress = 0.01f;
    }

    public boolean isCasting() {
        return casting;
    }

    public float getCastProgress() {
        return castProgress;
    }
}
