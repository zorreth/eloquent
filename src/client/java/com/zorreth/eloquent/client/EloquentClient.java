package com.zorreth.eloquent.client;

import com.zorreth.eloquent.SpeakPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EloquentClient implements ClientModInitializer {
    public static final String MOD_ID = "eloquent";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        ClientPlayNetworking.registerGlobalReceiver(SpeakPayload.TYPE, (payload, _) -> {
            LOGGER.info("Starting TTS: {}", payload.text());
            VoiceService.say(payload.text(), payload.volume());
        });
    }
}
