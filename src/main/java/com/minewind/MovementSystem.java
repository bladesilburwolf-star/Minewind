package com.minewind;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class MinewindMod implements ClientModInitializer {
    public static final String MOD_ID = "minewind";
    public static final String MOD_NAME = "Minewind";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MinewindMod instance;

    private final MorrowindSystems morrowindSystems;
    private final ModelSystem modelSystem;
    private final MovementSystem movementSystem;
    private final CombatSystem combatSystem;
    private final HUDSystem hudSystem;
    private final SpellSystem spellSystem;

    public MinewindMod() {
        instance = this;
        LOGGER.info("Initializing {} systems...", MOD_NAME);

        this.morrowindSystems = new MorrowindSystems();
        this.modelSystem = new ModelSystem();
        this.movementSystem = new MovementSystem();
        this.combatSystem = new CombatSystem();
        this.hudSystem = new HUDSystem();
        this.spellSystem = new SpellSystem();

        LOGGER.info("{} systems initialized successfully", MOD_NAME);
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Starting {} client initialization...", MOD_NAME);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> onClientStarting());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> onClientStopping());

        HudRenderCallback.EVENT.register((DrawContext drawContext, float tickDelta) -> {
            hudSystem.render(drawContext, tickDelta);
        });

        modelSystem.initialize();
        movementSystem.initialize();
        combatSystem.initialize();
        hudSystem.initialize();
        spellSystem.initialize();

        LOGGER.info("{} client initialization complete", MOD_NAME);
    }

    private void onClientStarting() {
        LOGGER.info("{} client starting", MOD_NAME);
        morrowindSystems.onClientStart();
    }

    private void onClientStopping() {
        LOGGER.info("{} client stopping", MOD_NAME);
        morrowindSystems.onClientStop();
    }

    public static MinewindMod getInstance() {
        return instance;
    }

    public MorrowindSystems getMorrowindSystems() {
        return morrowindSystems;
    }

    public ModelSystem getModelSystem() {
        return modelSystem;
    }

    public MovementSystem getMovementSystem() {
        return movementSystem;
    }

    public CombatSystem getCombatSystem() {
        return combatSystem;
    }

    public HUDSystem getHudSystem() {
        return hudSystem;
    }

    public SpellSystem getSpellSystem() {
        return spellSystem;
    }
}
