package com.zorreth.eloquent;

import com.zorreth.eloquent.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Eloquent implements ModInitializer {
    public static final String MOD_ID = "eloquent";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        PayloadTypeRegistry.clientboundPlay().register(SpeakPayload.TYPE, SpeakPayload.CODEC);

        ConfigManager.init();
        EloquentCommands.register();
        GenerativeService.reloadClient();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
