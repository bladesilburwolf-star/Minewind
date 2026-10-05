package com.minewind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinewindMod implements net.fabricmc.api.ModInitializer {
    public static final String MOD_ID = "minewind";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MinewindMod instance;
    private final MorrowindSystems morrowindSystems = new MorrowindSystems();

    public MinewindMod() {
        instance = this;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Minewind starting initialization");
        morrowindSystems.initialize();
        LOGGER.info("Minewind initialized successfully");
    }

    public static MinewindMod getInstance() {
        return instance;
    }

    public MorrowindSystems getMorrowindSystems() {
        return morrowindSystems;
    }
}
