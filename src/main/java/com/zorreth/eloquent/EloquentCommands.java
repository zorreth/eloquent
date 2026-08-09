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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
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

                                        Entity target = getLookedAtEntity(player);
                                        if (target == null) {
                                            target = getClosestEntity(player);
                                        }

                                        final String systemPrompt;
                                        final String mobName;
                                        final Entity speakingEntity = target;

                                        if (target == null) {
                                            mobName = ConfigManager.INSTANCE.model;
                                            systemPrompt = """
                                                    You are a helpful Minecraft assistant.
                                                    Answer in Minecraft chat style: keep responses short, no markdown.
                                                    """;
                                        } else {
                                            mobName = target.getName().getString();
                                            systemPrompt = """
                                                    You are a Minecraft %s.
                                                    Act like it. Keep responses short and do not use markdown.
                                                    """.formatted(mobName);
                                        }

                                        source.sendSuccess(() -> Component.literal(player.getPlainTextName() + ": " + message), false);

                                        CompletableFuture
                                                .supplyAsync(() -> GenerativeService.generateAsync(systemPrompt, message))
                                                .thenAccept(aiReply -> player.level().getServer().execute(() -> {
                                                    if (player.isRemoved() || player.hasDisconnected()) {
                                                        return;
                                                    }

                                                    List<ServerPlayer> players = getNearbyPlayers(player);

                                                    for (ServerPlayer p : players) {
                                                        float volume = 1.0f;

                                                        if (speakingEntity != null) {
                                                            double distance = p.distanceTo(speakingEntity);
                                                            volume = distanceToVolume(distance);
                                                        }

                                                        if (ServerPlayNetworking.canSend(p, SpeakPayload.TYPE)) {
                                                            ServerPlayNetworking.send(p, new SpeakPayload(aiReply, volume));
                                                        }

                                                        p.sendSystemMessage(Component.literal(mobName + ": " + aiReply));
                                                    }
                                                }))
                                                .exceptionally(ex -> {
                                                    player.level().getServer().execute(() -> source.sendFailure(Component.literal("AI request failed: " + ex.getMessage())));
                                                    return null;
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

    private static Entity getLookedAtEntity(ServerPlayer player) {
        double maxDistance = 6.0;

        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eyePos.add(look.scale(maxDistance));

        AABB searchBox = player.getBoundingBox()
                .expandTowards(look.scale(maxDistance))
                .inflate(1.0);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player,
                eyePos,
                end,
                searchBox,
                entity -> !entity.isSpectator() && entity.isAlive(),
                maxDistance * maxDistance
        );

        return hit != null ? hit.getEntity() : null;
    }

    private static Entity getClosestEntity(ServerPlayer player) {
        Vec3 playerPos = player.position();

        double maxDistance = 15.0;
        double maxDistSq = maxDistance * maxDistance;

        AABB searchBox = player.getBoundingBox().inflate(maxDistance);

        Entity closest = null;
        double closestDistSq = maxDistSq;

        for (Entity entity : player.level().getEntities(player, searchBox,
                e -> e.isAlive() && e != player && e.isPickable())) {

            double distSq = entity.distanceToSqr(playerPos);

            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = entity;
            }
        }

        return closest;
    }

    private static List<ServerPlayer> getNearbyPlayers(ServerPlayer origin) {
        List<ServerPlayer> result = new ArrayList<>();
        Vec3 originPos = origin.position();

        double maxDistance = 15.0;
        double maxDistSq = maxDistance * maxDistance;

        for (ServerPlayer player : origin.level().players()) {
            double distSq = player.distanceToSqr(originPos);
            if (distSq <= maxDistSq) {
                result.add(player);
            }
        }

        return result;
    }

    private static float distanceToVolume(double distance) {
        if (distance >= 20.0) return 0.0f;
        if (distance <= 0.0) return 1.0f;

        double t = distance / 20.0;
        return (float) (1.0 - t * t);
    }
}
