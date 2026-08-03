package com.zorreth.eloquent.client;

import com.zorreth.eloquent.client.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;

public class EloquentClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        ConfigManager.init();
    }
}
