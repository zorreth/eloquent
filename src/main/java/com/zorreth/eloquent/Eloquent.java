package com.zorreth.eloquent;

import com.zorreth.eloquent.config.ConfigManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class Eloquent implements ModInitializer {
    public static final String MOD_ID = "eloquent";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
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

        UseEntityCallback.EVENT.register((player, _, hand, _, _) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                if (hand == InteractionHand.MAIN_HAND) {
                    CompletableFuture.supplyAsync(() -> {
                        String systemPrompt = "You are a pig in Minecraft. Keep your responses short and clean, make appropriate sounds.";
                        return GenerativeService.generateAsync(systemPrompt, "Player interacted with you");
                    }).thenAccept(aiReply -> {
                        SpeakPayload payload = new SpeakPayload(aiReply);
                        ServerPlayNetworking.send(serverPlayer, payload);
                    });
                }
            }

            return InteractionResult.PASS;
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
