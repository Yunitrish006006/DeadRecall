package com.adaptor.deadrecall;

import com.adaptor.deadrecall.bootstrap.DeadRecallServerBootstrap;
import com.adaptor.deadrecall.bootstrap.TotemDiscordBridgeBootstrap;
import com.adaptor.deadrecall.network.registration.DeadRecallPayloadRegistration;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Deadrecall implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("DeadRecall");
    private static final int BOOKSHELF_REPLACE_INTERVAL_TICKS = 20;
    private static final int DISCORD_HEALTH_SAMPLE_INTERVAL_TICKS = 20 * 10;
    private static final int DISCORD_LOW_TPS_REQUIRED_SAMPLES = 3;
    private static final double DISCORD_LOW_TPS_THRESHOLD = 15.0D;
    private static final double DISCORD_RECOVERED_TPS_THRESHOLD = 18.0D;
    private static int bookshelfReplaceTicker = 0;
    private static MinecraftServer discordStatusOpenServer = null;
    private static long discordHealthTickStartNanos = 0L;
    private static int discordHealthSampleTicker = 0;
    private static int discordLowTpsSamples = 0;
    private static double discordAverageTickMillis = 50.0D;
    private static boolean discordLowTpsAlertActive = false;

    @Override
    public void onInitialize() {
        DeadRecallServerBootstrap.register(FabricLoader.getInstance().getConfigDir());
        DeadRecallPayloadRegistration.register();

        // 註冊死亡背包功能 - 當玩家死亡時收集掉落物品
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!FabricLoader.getInstance().isModLoaded("totem-discord-bridge")) {
                TotemDiscordBridgeBootstrap.onEntityDeath(entity, damageSource.getEntity());
            }
            if (entity instanceof ServerPlayer player) {
                DeathLocationManager.setDeathLocation(player, player.blockPosition(), player.level());
            }
        });

        // Discord owns its chat, lifecycle and health hooks through TotemDiscordBridgeBootstrap.

        // 生存模式不允許持有一般書櫃：統一替換為書本（每個書櫃 3 本）
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            bookshelfReplaceTicker++;
            if (bookshelfReplaceTicker < BOOKSHELF_REPLACE_INTERVAL_TICKS) {
                return;
            }
            bookshelfReplaceTicker = 0;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.getAbilities().instabuild) {
                    continue;
                }
                replaceVanillaBookshelfInInventory(player);
            }
        });

        // 註冊 /back 指令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("back")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    DeathLocationManager.DeathLocation loc = DeathLocationManager.getDeathLocation(player);
                    if (loc == null) {
                        player.sendSystemMessage(Component.translatable("message.deadrecall.back.no_position").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    ServerLevel world = context.getSource().getServer().getLevel(loc.dimension);
                    if (world == null) {
                        player.sendSystemMessage(Component.translatable("message.deadrecall.back.no_world").withStyle(ChatFormatting.RED));
                        return 0;
                    }
                    player.teleportTo(world, loc.pos.getX() + 0.5, loc.pos.getY(), loc.pos.getZ() + 0.5, Relative.DELTA, player.getYRot(), player.getXRot(), false);
                    player.sendSystemMessage(Component.translatable("message.deadrecall.back.success").withStyle(ChatFormatting.GREEN));
                    DeathLocationManager.clearDeathLocation(player);
                    return 1;
                })
            );

            if (!FabricLoader.getInstance().isModLoaded("totem-discord-bridge")) {
                TotemDiscordBridgeBootstrap.registerCommands(dispatcher);
            }
        });
    }

    public static void notifyServerOpened(MinecraftServer server, boolean immediate) {
        if (server == null) {
            return;
        }

        synchronized (Deadrecall.class) {
            if (discordStatusOpenServer == server) {
                return;
            }
            discordStatusOpenServer = server;
        }

        sendDiscordServerStatus(server, "伺服器已開啟", 20.0D, immediate);
    }

    public static void notifyServerClosed(MinecraftServer server, boolean immediate) {
        if (server == null) {
            return;
        }

        synchronized (Deadrecall.class) {
            if (discordStatusOpenServer != server) {
                return;
            }
            discordStatusOpenServer = null;
        }

        sendDiscordServerStatus(server, "伺服器已關閉", 0.0D, immediate);
    }

    private static void sendDiscordServerStatus(MinecraftServer server, String status, double tps, boolean immediate) {
        int playersOnline = server.getPlayerList().getPlayerCount();
        int playersMax = server.getPlayerList().getMaxPlayers();
        String version = server.getServerVersion();

        if (immediate) {
            DiscordBridge.sendServerStatusImmediately(status, playersOnline, playersMax, version, tps);
        } else {
            DiscordBridge.sendServerStatus(status, playersOnline, playersMax, version, tps);
        }
    }

    private static boolean isFirstJoin(ServerPlayer player) {
        return player.getStats().getValue(Stats.CUSTOM, Stats.PLAY_TIME) <= 0;
    }

    private static String damageSourcePlayerName(Entity sourceEntity) {
        return sourceEntity instanceof ServerPlayer player ? player.getName().getString() : "";
    }

    private static void trackDiscordHealthTickStart(MinecraftServer server) {
        discordHealthTickStartNanos = System.nanoTime();
    }

    private static void trackDiscordHealthTickEnd(MinecraftServer server) {
        if (discordHealthTickStartNanos <= 0L) {
            return;
        }

        long elapsedNanos = System.nanoTime() - discordHealthTickStartNanos;
        double elapsedMillis = elapsedNanos / 1_000_000.0D;
        discordAverageTickMillis = (discordAverageTickMillis * 0.95D) + (elapsedMillis * 0.05D);
        discordHealthSampleTicker++;
        if (discordHealthSampleTicker < DISCORD_HEALTH_SAMPLE_INTERVAL_TICKS) {
            return;
        }

        discordHealthSampleTicker = 0;
        double tps = Math.min(20.0D, 1000.0D / Math.max(1.0D, discordAverageTickMillis));
        if (tps < DISCORD_LOW_TPS_THRESHOLD) {
            discordLowTpsSamples++;
            if (!discordLowTpsAlertActive && discordLowTpsSamples >= DISCORD_LOW_TPS_REQUIRED_SAMPLES) {
                discordLowTpsAlertActive = true;
                DiscordBridge.sendServerHealthAlert(String.format(Locale.ROOT, "TPS 持續偏低：%.1f TPS", tps));
            }
            return;
        }

        discordLowTpsSamples = 0;
        if (discordLowTpsAlertActive && tps >= DISCORD_RECOVERED_TPS_THRESHOLD) {
            discordLowTpsAlertActive = false;
            DiscordBridge.sendServerHealthAlert(String.format(Locale.ROOT, "TPS 已恢復：%.1f TPS", tps));
        }
    }

    private void replaceVanillaBookshelfInInventory(ServerPlayer player) {
        boolean changed = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(Items.BOOKSHELF)) {
                continue;
            }
            int booksToGive = stack.getCount() * 3;
            player.getInventory().setItem(slot, ItemStack.EMPTY);
            while (booksToGive > 0) {
                int batch = Math.min(booksToGive, Items.BOOK.getDefaultMaxStackSize());
                ItemStack books = new ItemStack(Items.BOOK, batch);
                if (!player.getInventory().add(books)) {
                    player.drop(books, false);
                }
                booksToGive -= batch;
            }
            changed = true;
        }
        if (changed) {
            player.getInventory().setChanged();
        }
    }

}
