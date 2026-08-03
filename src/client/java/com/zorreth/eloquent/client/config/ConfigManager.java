package com.zorreth.eloquent.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    public static final String MOD_ID = "eloquent";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;

    public static EloquentConfig INSTANCE = new EloquentConfig();

    public static class EloquentConfig {
        public String baseUrl = "https://api.openai.com/v1";
        public String model = "gpt-5.6";
        public String apiKey = "";
    }

    public static void init() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve("eloquent.json");

        try {
            Files.createDirectories(configPath.getParent());
        } catch (Exception e) {
            LOGGER.error("Failed to create config directory for Eloquent!", e);
        }

        load();
    }

    public static void load() {
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                INSTANCE = GSON.fromJson(reader, EloquentConfig.class);
            } catch (IOException e) {
                LOGGER.error("Failed to load Eloquent config!", e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save Eloquent config!", e);
        }
    }
}
