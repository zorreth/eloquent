package com.zorreth.eloquent.client.mixin;

import com.zorreth.eloquent.client.config.ConfigScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addConfigButton(CallbackInfo info) {
        Button configButton = Button.builder(Component.literal("Eloquent"), _ -> {
            this.minecraft.setScreenAndShow(new ConfigScreen(this));
        }).bounds(10, 10, 80, 20).build();

        this.addRenderableWidget(configButton);
    }
}
