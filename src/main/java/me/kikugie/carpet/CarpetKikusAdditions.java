package me.kikugie.carpet;

import carpet.CarpetExtension;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarpetKikusAdditions implements CarpetExtension, ModInitializer {
    private static final String MOD_ID = "kikus-carpet-additions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Kiku's Carpet Additions");
    }
}
