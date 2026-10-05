package com.minewind;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinewindMod implements ModInitializer {
    public static final String MOD_ID = "minewind";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.register();
        MorrowindWeapons.register();
        ModEntities.register();
        LOGGER.info("Minewind loaded");
    }
}
