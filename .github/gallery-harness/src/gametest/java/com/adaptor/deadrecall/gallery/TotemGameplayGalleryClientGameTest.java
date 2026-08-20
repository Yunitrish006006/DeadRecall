package com.adaptor.deadrecall.gallery;

import dev.totem.discord.client.DiscordConfigScreen;
import dev.totem.discord.network.DiscordConfigSyncPayload;
import dev.totem.enchanting.power.EnchantingPowerHelper;
import dev.totem.excavation.registry.ExcavationItems;
import dev.totem.excavation.selection.HammerSelectionService;
import dev.totem.excavation.session.ExcavationSessions;
import dev.totem.vanillatweaks.inventory.ContainerSortService;
import dev.totem.vanillatweaks.network.SortBackpackPayload;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Produces fresh, real Minecraft client renders from the exact DeadRecall 2.4.21
 * standalone module graph. It supplements the modules that did not already
 * ship a dedicated client visual suite.
 */
@SuppressWarnings("UnstableApiUsage")
public final class TotemGameplayGalleryClientGameTest implements FabricClientGameTest {
    private static final int Y = 70;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getServer().runCommand("execute in minecraft:overworld run time set 1000");
            singleplayer.getServer().runCommand("execute in minecraft:overworld run weather clear");
            singleplayer.getServer().runCommand("execute in minecraft:overworld run gamemode creative @a");
            selectEnglish(context);

            captureCoreEcosystem(context, singleplayer);
            captureEnchanting(context, singleplayer);
            captureExcavation(context, singleplayer);
            captureVanillaTweaks(context, singleplayer);
            captureDiscordBridge(context, singleplayer);
        }
    }

    private static void captureCoreEcosystem(ClientGameTestContext context, TestSingleplayerContext singleplayer) {
        int x = 0;
        preparePlatform(singleplayer, x, 22);
        singleplayer.getServer().computeOnServer(server -> {
            var level = server.overworld();
            level.setBlockAndUpdate(new BlockPos(x - 7, Y, 0), Blocks.CAMPFIRE.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(x - 7, Y + 1, 0), block("deadrecall:alchemy_cauldron").defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(x, Y, 0), Blocks.ENCHANTING_TABLE.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(x - 2, Y, 0), Blocks.CHISELED_BOOKSHELF.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(x + 7, Y, 0), block("totem:woodcutter").defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(x + 4, Y, 4), Blocks.CHEST.defaultBlockState());
            ServerPlayer player = firstPlayer(server);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("totem:automata/copper_wrench")));
            player.getInventory().setItem(9, new ItemStack(item("totem:remnant/backpack_advanced")));
            player.getInventory().setItem(10, new ItemStack(item("totem:locksmith/padlock")));
            player.getInventory().setItem(11, new ItemStack(item("totem:excavation/diamond_hammer")));
            player.inventoryMenu.broadcastChanges();
            return null;
        });
        camera(context, singleplayer, x + 15.0, Y + 8.0, 15.0,
                new Vec3(x, Y + 1.2, 0), "gallery-totem-core-ecosystem-hero", true);

        context.runOnClient(client -> client.setScreenAndShow(new net.minecraft.client.gui.screens.inventory.InventoryScreen(client.player)));
        context.waitTicks(4);
        context.takeScreenshot("gallery-totem-core-cross-module-inventory");
        context.runOnClient(client -> client.setScreenAndShow(null));
        context.waitTicks(2);
    }

    private static void captureEnchanting(ClientGameTestContext context, TestSingleplayerContext singleplayer) {
        int x = 80;
        preparePlatform(singleplayer, x, 18);
        BlockPos table = new BlockPos(x, Y, 0);
        List<BlockPos> shelves = List.of(
                table.offset(-2, 0, 0), table.offset(2, 0, 0),
                table.offset(0, 0, -2), table.offset(0, 0, 2),
                table.offset(-2, 0, -1), table.offset(2, 0, 1)
        );

        singleplayer.getServer().computeOnServer(server -> {
            var level = server.overworld();
            level.setBlockAndUpdate(table, Blocks.ENCHANTING_TABLE.defaultBlockState());
            for (BlockPos shelfPos : shelves) {
                level.setBlockAndUpdate(shelfPos, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
            }
            require(EnchantingPowerHelper.calculateBookPower(level, table) == 0,
                    "Empty chiseled bookshelves unexpectedly supplied enchanting power");
            return null;
        });
        camera(context, singleplayer, x + 10.0, Y + 5.5, 12.0,
                new Vec3(x, Y + 0.8, 0), "gallery-totem-enchanting-empty-shelves", true);

        singleplayer.getServer().computeOnServer(server -> {
            var level = server.overworld();
            Holder<Enchantment> sharpness = level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.SHARPNESS);
            for (int i = 0; i < shelves.size(); i++) {
                BlockEntity entity = level.getBlockEntity(shelves.get(i));
                require(entity instanceof ChiseledBookShelfBlockEntity,
                        "Configured chiseled bookshelf did not expose its block entity");
                ChiseledBookShelfBlockEntity shelf = (ChiseledBookShelfBlockEntity) entity;
                for (int slot = 0; slot < shelf.getContainerSize(); slot++) {
                    ItemStack stack = (slot + i) % 3 == 0 ? new ItemStack(Items.ENCHANTED_BOOK) : new ItemStack(Items.BOOK);
                    if (stack.is(Items.ENCHANTED_BOOK)) {
                        stack.enchant(sharpness, 3);
                    }
                    shelf.setItem(slot, stack);
                }
                shelf.setChanged();
            }
            require(EnchantingPowerHelper.calculateBookPower(level, table) > 0,
                    "Filled chiseled bookshelves did not supply weighted enchanting power");
            return null;
        });
        context.waitTicks(4);
        camera(context, singleplayer, x + 10.0, Y + 5.5, 12.0,
                new Vec3(x, Y + 0.8, 0), "gallery-totem-enchanting-weighted-shelves", true);
        camera(context, singleplayer, x - 8.0, Y + 2.4, 5.0,
                new Vec3(x, Y + 0.8, 0), "gallery-totem-enchanting-bookshelf-detail", false);
    }

    private static void captureExcavation(ClientGameTestContext context, TestSingleplayerContext singleplayer) {
        int x = 160;
        preparePlatform(singleplayer, x, 24);
        BlockPos first = new BlockPos(x - 4, Y, -4);
        BlockPos second = new BlockPos(x + 4, Y + 2, 4);

        singleplayer.getServer().computeOnServer(server -> {
            var level = server.overworld();
            for (BlockPos pos : BlockPos.betweenClosed(first, second)) {
                level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
            }
            ServerPlayer player = firstPlayer(server);
            ItemStack hammer = new ItemStack(ExcavationItems.DIAMOND_HAMMER);
            player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
            HammerSelectionService.select(player, InteractionHand.MAIN_HAND, hammer,
                    ExcavationItems.DIAMOND_HAMMER, level, first, true);
            HammerSelectionService.select(player, InteractionHand.MAIN_HAND, hammer,
                    ExcavationItems.DIAMOND_HAMMER, level, second, false);
            player.inventoryMenu.broadcastChanges();
            return null;
        });
        context.waitTicks(4);
        camera(context, singleplayer, x + 13.0, Y + 7.0, 13.0,
                new Vec3(x, Y + 1.0, 0), "gallery-totem-excavation-selected-area", false);

        singleplayer.getServer().computeOnServer(server -> {
            ServerPlayer player = firstPlayer(server);
            ItemStack held = player.getMainHandItem();
            var level = server.overworld();
            level.destroyBlock(first, true, player);
            ExcavationSessions.startAfterManualBreak(player, level, held, first);
            return null;
        });
        context.waitTicks(2);
        camera(context, singleplayer, x + 13.0, Y + 7.0, 13.0,
                new Vec3(x, Y + 1.0, 0), "gallery-totem-excavation-in-progress", false);
        context.waitTicks(50);
        camera(context, singleplayer, x + 13.0, Y + 7.0, 13.0,
                new Vec3(x, Y + 1.0, 0), "gallery-totem-excavation-complete", false);

        singleplayer.getServer().computeOnServer(server -> {
            ServerPlayer player = firstPlayer(server);
            List<Item> hammers = List.of(
                    ExcavationItems.WOODEN_HAMMER, ExcavationItems.STONE_HAMMER,
                    ExcavationItems.COPPER_HAMMER, ExcavationItems.IRON_HAMMER,
                    ExcavationItems.GOLDEN_HAMMER, ExcavationItems.DIAMOND_HAMMER,
                    ExcavationItems.NETHERITE_HAMMER
            );
            for (int i = 0; i < hammers.size(); i++) {
                player.getInventory().setItem(9 + i, new ItemStack(hammers.get(i)));
            }
            player.inventoryMenu.broadcastChanges();
            return null;
        });
        context.runOnClient(client -> client.setScreenAndShow(new net.minecraft.client.gui.screens.inventory.InventoryScreen(client.player)));
        context.waitTicks(4);
        context.takeScreenshot("gallery-totem-excavation-hammer-tiers");
        context.runOnClient(client -> client.setScreenAndShow(null));
        context.waitTicks(2);
    }

    private static void captureVanillaTweaks(ClientGameTestContext context, TestSingleplayerContext singleplayer) {
        int x = 240;
        preparePlatform(singleplayer, x, 20);
        BlockPos chestPos = new BlockPos(x, Y, 0);
        singleplayer.getServer().computeOnServer(server -> {
            var level = server.overworld();
            level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
            BlockEntity entity = level.getBlockEntity(chestPos);
            require(entity instanceof ChestBlockEntity, "Gallery chest did not create a chest block entity");
            ChestBlockEntity chest = (ChestBlockEntity) entity;
            chest.setItem(0, new ItemStack(Items.DIAMOND, 3));
            chest.setItem(1, new ItemStack(Items.APPLE, 12));
            chest.setItem(2, new ItemStack(Items.COBBLESTONE, 32));
            chest.setItem(3, new ItemStack(Items.APPLE, 20));
            chest.setItem(4, new ItemStack(Items.IRON_INGOT, 7));
            chest.setItem(8, new ItemStack(Items.DIRT, 16));
            ServerPlayer player = firstPlayer(server);
            player.setPos(x, Y, 3);
            player.openMenu(chest);
            return null;
        });
        context.waitTicks(6);
        context.takeScreenshot("gallery-totem-vanilla-tweaks-container-before-sort");
        singleplayer.getServer().computeOnServer(server -> {
            ServerPlayer player = firstPlayer(server);
            require(ContainerSortService.sortOpenContainer(player, SortBackpackPayload.Target.CONTAINER),
                    "Container sort service rejected the showcase chest");
            return null;
        });
        context.waitTicks(5);
        context.takeScreenshot("gallery-totem-vanilla-tweaks-container-after-sort");
        context.runOnClient(client -> client.setScreenAndShow(null));
        context.waitTicks(2);

        BlockPos water = new BlockPos(x + 7, Y, 0);
        final ItemEntity[] powder = new ItemEntity[1];
        singleplayer.getServer().computeOnServer(server -> {
            var level = server.overworld();
            level.setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
            level.setBlockAndUpdate(water.below(), Blocks.STONE.defaultBlockState());
            ItemEntity entity = new ItemEntity(level, x + 4.5, Y + 0.2, 0.5,
                    new ItemStack(Items.RED_CONCRETE_POWDER, 16));
            entity.setDeltaMovement(0, 0, 0);
            level.addFreshEntity(entity);
            powder[0] = entity;
            return null;
        });
        camera(context, singleplayer, x + 10.0, Y + 3.0, 7.0,
                new Vec3(x + 5.0, Y + 0.5, 0.5), "gallery-totem-vanilla-tweaks-concrete-powder-before", false);
        singleplayer.getServer().computeOnServer(server -> {
            ItemEntity entity = powder[0];
            require(entity != null && entity.isAlive(), "Concrete powder showcase entity disappeared before water contact");
            entity.setPos(water.getX() + 0.5, water.getY() + 0.2, water.getZ() + 0.5);
            entity.setDeltaMovement(0, 0, 0);
            return null;
        });
        context.waitTicks(8);
        camera(context, singleplayer, x + 10.0, Y + 3.0, 7.0,
                new Vec3(water.getX() + 0.5, water.getY() + 0.5, water.getZ() + 0.5),
                "gallery-totem-vanilla-tweaks-concrete-hardened", false);

        singleplayer.getServer().runCommand("execute in minecraft:overworld run gamemode survival @a");
        singleplayer.getServer().computeOnServer(server -> {
            ServerPlayer player = firstPlayer(server);
            player.getInventory().clearContent();
            player.getInventory().add(new ItemStack(Items.BOOKSHELF, 2));
            player.inventoryMenu.broadcastChanges();
            return null;
        });
        context.waitTicks(8);
        context.runOnClient(client -> client.setScreenAndShow(new net.minecraft.client.gui.screens.inventory.InventoryScreen(client.player)));
        context.waitTicks(4);
        context.takeScreenshot("gallery-totem-vanilla-tweaks-bookshelf-survival-conversion");
        context.runOnClient(client -> client.setScreenAndShow(null));
        singleplayer.getServer().runCommand("execute in minecraft:overworld run gamemode creative @a");
        context.waitTicks(2);
    }

    private static void captureDiscordBridge(ClientGameTestContext context, TestSingleplayerContext singleplayer) {
        context.runOnClient(client -> {
            DiscordConfigScreen screen = new DiscordConfigScreen();
            client.setScreenAndShow(screen);
            screen.applyServerConfig(true, "https://totem-bridge.example.workers.dev", "");
            screen.applyChannels(List.of(
                    new DiscordConfigSyncPayload.ChannelData("123456789012345678", "survival-log"),
                    new DiscordConfigSyncPayload.ChannelData("223456789012345678", "server-status"),
                    new DiscordConfigSyncPayload.ChannelData("323456789012345678", "achievements")
            ));
        });
        context.waitTicks(5);
        context.takeScreenshot("gallery-totem-discord-bridge-configured-channels");

        context.getInput().scroll(0, -8);
        context.waitTicks(3);
        context.takeScreenshot("gallery-totem-discord-bridge-channel-management");
        context.runOnClient(client -> client.setScreenAndShow(null));
        context.waitTicks(2);

        int x = 320;
        preparePlatform(singleplayer, x, 14);
        singleplayer.getServer().runCommand("execute in minecraft:overworld run title @a actionbar {\"text\":\"TotemDiscordBridge relays join, death, recovery, advancements and server status\",\"color\":\"aqua\"}");
        camera(context, singleplayer, x + 8.0, Y + 4.0, 8.0,
                new Vec3(x, Y + 1.0, 0), "gallery-totem-discord-bridge-live-server", false);
    }

    private static void preparePlatform(TestSingleplayerContext singleplayer, int centerX, int radius) {
        int minX = centerX - radius;
        int maxX = centerX + radius;
        singleplayer.getServer().runCommand("execute in minecraft:overworld run fill "
                + minX + " " + (Y - 1) + " -" + radius + " "
                + maxX + " " + (Y + 12) + " " + radius + " air");
        singleplayer.getServer().runCommand("execute in minecraft:overworld run fill "
                + minX + " " + (Y - 1) + " -" + radius + " "
                + maxX + " " + (Y - 1) + " " + radius + " smooth_stone");
    }

    private static void camera(ClientGameTestContext context, TestSingleplayerContext singleplayer,
                               double x, double y, double z, Vec3 target,
                               String screenshot, boolean hideHud) {
        singleplayer.getServer().runCommand("execute in minecraft:overworld run tp @a " + x + " " + y + " " + z);
        context.waitTicks(5);
        singleplayer.getClientLevel().waitForChunksRender();
        context.runOnClient(client -> {
            if (client.player == null) {
                throw new AssertionError("Gallery camera player is unavailable");
            }
            client.player.setPosRaw(x, y, z);
            client.player.lookAt(EntityAnchorArgument.Anchor.EYES, target);
            client.player.xo = client.player.getX();
            client.player.yo = client.player.getY();
            client.player.zo = client.player.getZ();
            client.player.yRotO = client.player.getYRot();
            client.player.xRotO = client.player.getXRot();
        });
        if (hideHud) {
            context.getInput().pressKey(GLFW.GLFW_KEY_F1);
        }
        context.waitTicks(3);
        context.takeScreenshot(screenshot);
        if (hideHud) {
            context.getInput().pressKey(GLFW.GLFW_KEY_F1);
        }
        context.waitTicks(2);
    }

    private static ServerPlayer firstPlayer(net.minecraft.server.MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        require(!players.isEmpty(), "Gallery world has no server player");
        return players.getFirst();
    }

    private static Item item(String id) {
        Item value = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
        require(value != null && value != Items.AIR, "Missing gallery item: " + id);
        return value;
    }

    private static Block block(String id) {
        Block value = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
        require(value != null && value != Blocks.AIR, "Missing gallery block: " + id);
        return value;
    }

    private static void selectEnglish(ClientGameTestContext context) {
        context.runOnClient(client -> {
            client.options.languageCode = "en_us";
            client.getLanguageManager().setSelected("en_us");
            client.getLanguageManager().onResourceManagerReload(client.getResourceManager());
        });
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
