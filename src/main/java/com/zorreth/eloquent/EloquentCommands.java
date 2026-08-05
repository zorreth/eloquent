package com.zorreth.eloquent;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.zorreth.eloquent.config.ConfigManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EloquentCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, _, _) -> {
            LiteralCommandNode<CommandSourceStack> eloquentNode = dispatcher.register(Commands.literal("eloquent")
                    .then(Commands.literal("setkey")
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER))
                            .then(Commands.argument("key", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String apiKey = StringArgumentType.getString(context, "key");

                                        ConfigManager.INSTANCE.apiKey = apiKey;
                                        ConfigManager.save();

                                        context.getSource().sendSuccess(() ->
                                                Component.literal("§a[Eloquent] API Key updated to: §f" + apiKey), false);

                                        if (apiKey.length() > 200) {
                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("§e[Eloquent] Warning: Your API key is very long - please check if it has been pasted fully. If not, you might want to edit the eloquent.json file directly."), false);
                                        }

                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(Commands.literal("setmodel")
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER))
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
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER))
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
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER))
                            .executes(context -> {
                                ConfigManager.load();

                                context.getSource().sendSuccess(() ->
                                        Component.literal("§a[Eloquent] Configuration reloaded."), false);

                                try {
                                    GenerativeService.reloadClient();

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
                    .then(Commands.literal("chat")
                            .then(Commands.argument("message", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String message = StringArgumentType.getString(context, "message");
                                        CommandSourceStack source = context.getSource();

                                        ServerPlayer player = source.getPlayerOrException();
                                        ServerLevel level = player.level();

                                        AABB searchBox = player.getBoundingBox().inflate(15.0D);

                                        List<Mob> nearbyMobs = level.getEntitiesOfClass(Mob.class, searchBox,
                                                mob -> player.distanceTo(mob) <= 15.0F
                                        );

                                        final String systemPrompt;
                                        final String mobName;

                                        if (!nearbyMobs.isEmpty()) {
                                            int randomIndex = player.getRandom().nextInt(nearbyMobs.size());
                                            Mob randomMob = nearbyMobs.get(randomIndex);
                                            mobName = randomMob.getPlainTextName();

                                            systemPrompt = "You are a Minecraft " + mobName + ". Act like it, keep your responses short and don't use markdown formatting.";
                                        } else {
                                            mobName = ConfigManager.INSTANCE.model;
                                            systemPrompt = "You are a helpful Minecraft assistant. You answer in a Minecraft chat, so keep your responses short and don't use markdown formatting.";
                                        }

                                        CompletableFuture
                                                .supplyAsync(() -> GenerativeService.generateAsync(systemPrompt, message))
                                                .thenAccept(aiReply -> {
                                                    SpeakPayload payload = new SpeakPayload(aiReply);
                                                    ServerPlayNetworking.send(player, payload);

                                                    context.getSource().sendSuccess(() ->
                                                            Component.literal(mobName + ": " + aiReply), false);
                                                });

                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
            );

            CommandNode<CommandSourceStack> chatNode = eloquentNode.getChild("chat");

            dispatcher.register(Commands.literal("e").redirect(chatNode));
        }));
    }
}
