package com.zorreth.eloquent.client.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class ConfigScreen extends Screen {
    public Screen parent;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Eloquent"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        EditBox baseUrlField = new EditBox(this.font, this.width / 2 - 100, 50, 200, 20, Component.literal("Base URL"));
        baseUrlField.setHint(Component.literal("Base URL"));
        baseUrlField.setValue(ConfigManager.INSTANCE.baseUrl);
        this.addRenderableWidget(baseUrlField);

        EditBox modelField = new EditBox(this.font, this.width / 2 - 100, 80, 200, 20, Component.literal("Model"));
        modelField.setHint(Component.literal("Model"));
        modelField.setValue(ConfigManager.INSTANCE.model);
        this.addRenderableWidget(modelField);

        EditBox apiKeyField = new EditBox(this.font, this.width / 2 - 100, 110, 200, 20, Component.literal("API Key"));
        apiKeyField.setHint(Component.literal("API Key"));
        apiKeyField.setValue(ConfigManager.INSTANCE.apiKey);
        this.addRenderableWidget(apiKeyField);

        Button closeButtonWidget = Button.builder(Component.literal("Close"), _ -> {
            this.onClose();
        }).bounds(this.width / 2 - 100, 140, 95, 20).build();
        this.addRenderableWidget(closeButtonWidget);

        Button saveButtonWidget = Button.builder(Component.literal("Save"), _ -> {
            ConfigManager.INSTANCE.baseUrl = baseUrlField.getValue();
            ConfigManager.INSTANCE.model = modelField.getValue();
            ConfigManager.INSTANCE.apiKey = apiKeyField.getValue();

            ConfigManager.save();

            this.onClose();
        }).bounds(this.width / 2 + 5, 140, 95, 20).build();
        this.addRenderableWidget(saveButtonWidget);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int textWidth = this.font.width(this.title);
        int centeredX = (this.width / 2) - (textWidth / 2);

        graphics.text(this.font, this.title, centeredX, 15, 0xFFFFFFFF, true);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
