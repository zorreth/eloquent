package com.zorreth.eloquent;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.zorreth.eloquent.config.ConfigManager;

public class AiManager {
    private static OpenAIClient openAIClient;

    public static void reloadClient() {
        String apiKey = ConfigManager.INSTANCE.apiKey;
        String baseUrl = ConfigManager.INSTANCE.baseUrl;

        if (apiKey == null || apiKey.isEmpty()) {
            Eloquent.LOGGER.warn("No API key set! AI features are paused.");
            openAIClient = null;
            return;
        }

        openAIClient = OpenAIOkHttpClient.builder().apiKey(apiKey).baseUrl(baseUrl).build();
        Eloquent.LOGGER.info("OpenAI client successfully initialized.");
    }

    public static OpenAIClient getClient() {
        return openAIClient;
    }
}
