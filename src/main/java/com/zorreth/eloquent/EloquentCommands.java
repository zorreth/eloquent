package com.zorreth.eloquent;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.zorreth.eloquent.config.ConfigManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

public class EloquentCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, _, _) -> {
            dispatcher.register(Commands.literal("eloquent")
                    .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER))
                    .then(Commands.literal("setkey")
                            .then(Commands.argument("key", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        ConfigManager.INSTANCE.apiKey = StringArgumentType.getString(context, "key");
                                        ConfigManager.save();

                                        context.getSource().sendSuccess(() ->
                                                Component.literal("§a[Eloquent] API Key updated successfully!"), false);
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(Commands.literal("setmodel")
                            .then(Commands.argument("model", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String model = StringArgumentType.getString(context, "model");

                                        ConfigManager.INSTANCE.model = model;
                                        ConfigManager.save();

                                        context.getSource().sendSuccess(() ->
                                                Component.literal("§a[Eloquent] Model updated to: §f" + model), false);
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(Commands.literal("setbaseurl")
                            .then(Commands.argument("baseurl", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String baseURL = StringArgumentType.getString(context, "baseurl");

                                        ConfigManager.INSTANCE.baseUrl = baseURL;
                                        ConfigManager.save();

                                        context.getSource().sendSuccess(() ->
                                                Component.literal("§a[Eloquent] Base URL updated to: §f" + baseURL), false);
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(Commands.literal("reload")
                            .executes(context -> {
                                ConfigManager.load();

                                context.getSource().sendSuccess(() ->
                                        Component.literal("§a[Eloquent] Configuration reloaded."), false);

                                try {
                                    AiManager.reloadClient();

                                    context.getSource().sendSuccess(() ->
                                            Component.literal("§a[Eloquent] AI features enabled!"), false);
                                } catch (Exception e) {
                                    Eloquent.LOGGER.error("Failed to load OpenAI client!", e);
                                    context.getSource().sendFailure(
                                            Component.literal("§c[Eloquent] Failed to load OpenAI client! Check the server console for details.")
                                    );
                                }

                                return Command.SINGLE_SUCCESS;
                            })
                    )
                    .then(Commands.literal("config")
                            .executes(context -> {
                                context.getSource().sendSuccess(() ->
                                        Component.literal("§a[Eloquent] Loaded configuration:"), false);

                                context.getSource().sendSuccess(() ->
                                        Component.literal("§7 - Base URL: §f" + ConfigManager.INSTANCE.baseUrl), false);

                                context.getSource().sendSuccess(() ->
                                        Component.literal("§7 - API Key: §f" + ConfigManager.INSTANCE.apiKey), false);

                                context.getSource().sendSuccess(() ->
                                        Component.literal("§7 - Model: §f" + ConfigManager.INSTANCE.model), false);

                                return Command.SINGLE_SUCCESS;
                            })
                    )
            );
        }));
    }
}
