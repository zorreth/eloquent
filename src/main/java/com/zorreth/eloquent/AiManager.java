package com.zorreth.eloquent;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.errors.OpenAIServiceException;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
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

    public static String generateAsync(String systemPrompt, String userMessage) {
        if (openAIClient == null) {
            return "Error: OpenAI client is not initialized or API key is missing.";
        }

        try {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .addSystemMessage(systemPrompt)
                    .addUserMessage(userMessage)
                    .model(ConfigManager.INSTANCE.model)
                    .build();

            ChatCompletion completion = openAIClient.chat().completions().create(params);

            return completion.choices().getFirst().message().content().orElse("No response generated.");
        } catch (OpenAIServiceException e) {
            Eloquent.LOGGER.error("OpenAI API request failed with HTTP Status: {}", e.statusCode());
            Eloquent.LOGGER.error("OpenAI Error Body: {}", e.body());

            return "API Error: Received status code " + e.statusCode();
        } catch (Exception e) {
            Eloquent.LOGGER.error("Failed to generate AI response", e);
            return "Error: Failed to generate AI response";
        }
    }
}
