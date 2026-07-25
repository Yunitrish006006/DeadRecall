package com.adaptor.deadrecall.gametest;

import com.adaptor.deadrecall.bootstrap.AutomataCutover;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Opens a world seeded by the pre-cutover Copper Golem probe through the
 * exact compatibility bundle, then persists the same state through the
 * external Automata authority in a second Dedicated Server JVM.
 */
public final class LegacyAutomataMigrationProbe implements ModInitializer {
    private static final String PHASE_ENV = "DEADRECALL_LEGACY_AUTOMATA_MIGRATION_PHASE";
    private static final String LEGACY_MARKER_DIRECTORY_ENV = "DEADRECALL_LEGACY_AUTOMATA_LEGACY_MARKER_DIR";
    private static final String PROBE_MARKER_DIRECTORY_ENV = "DEADRECALL_LEGACY_AUTOMATA_PROBE_MARKER_DIR";
    private static final String LEGACY_UUID_FILE = "copper-golem.uuid";
    private static final String LEGACY_PROBE_MARKER = "deadrecall_copper_restart_probe";
    private static final BlockPos GOLEM_POS = new BlockPos(164, 200, 164);
    private static final BlockPos HOME_POS = GOLEM_POS.offset(2, 0, 0);
    private static final BlockPos TARGET_POS = GOLEM_POS.offset(4, 0, 0);
    private static final int CHUNK_X = SectionPos.blockToSectionCoord(GOLEM_POS.getX());
    private static final int CHUNK_Z = SectionPos.blockToSectionCoord(GOLEM_POS.getZ());
    private static final Component LEGACY_TOOL_NAME = Component.literal("Copper restart probe tool");
    private static final Component LEGACY_STORAGE_NAME = Component.literal("Copper restart probe storage");
    private static final Component MIGRATED_STORAGE_NAME = Component.literal("Recovered copper restart storage");

    @Override
    public void onInitialize() {
        String phase = System.getenv(PHASE_ENV);
        if (phase == null || phase.isBlank()) {
            return;
        }
        Path legacyMarkerDirectory = requiredDirectory(LEGACY_MARKER_DIRECTORY_ENV);
        Path probeMarkerDirectory = requiredDirectory(PROBE_MARKER_DIRECTORY_ENV);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            requireExternalAutomataAuthority();
            ServerLevel level = server.overworld();
            level.setChunkForced(CHUNK_X, CHUNK_Z, true);
            level.getChunk(CHUNK_X, CHUNK_Z);
            ServerTickEvents.END_SERVER_TICK.register(
                    new Session(phase, legacyMarkerDirectory, probeMarkerDirectory)::tick);
        });
    }

    private static void runPhase(MinecraftServer server, String phase, Path legacyMarkerDirectory) {
        ServerLevel level = server.overworld();
        switch (phase) {
            case "migrate" -> migrateLegacyState(level, legacyMarkerDirectory);
            case "verify" -> verifyMigratedState(level, legacyMarkerDirectory);
            default -> throw new IllegalArgumentException("Unknown legacy Automata migration phase: " + phase);
        }
    }

    private static void migrateLegacyState(ServerLevel level, Path legacyMarkerDirectory) {
        CopperGolem golem = requireGolem(level, readLegacyGolemId(legacyMarkerDirectory));
        verifyLegacySeedState(level, golem);

        CompoundTag tag = readEntityTag(golem);
        tag.putInt("deadrecall_revision", 78);
        tag.putString("deadrecall_activity", "blocked_home_unavailable");
        tag.putInt("deadrecall_fuel_ticks", 111);
        tag.putInt("deadrecall_gathering_target_y", TARGET_POS.getY() + 1);
        tag.putLong("deadrecall_gathering_scan_index", 144L);

        ItemStack tool = readItemStack(tag, "deadrecall_gathering_tool_stack");
        tool.setDamageValue(23);
        writeItemStack(tag, "deadrecall_gathering_tool_stack", tool);
        writeItemStack(tag, "deadrecall_gathering_storage_stack", namedStack(Items.COBBLESTONE, 7, MIGRATED_STORAGE_NAME));
        writeEntityTag(golem, tag);

        require(level.setBlockAndUpdate(TARGET_POS, Blocks.DEEPSLATE.defaultBlockState()),
                "External Automata authority could not update the legacy target block");
        Container home = homeContainer(level);
        home.setItem(0, new ItemStack(Items.DIAMOND, 5));
        home.setChanged();
        verifyMigratedState(level, legacyMarkerDirectory);
    }

    private static void verifyLegacySeedState(ServerLevel level, CopperGolem golem) {
        CompoundTag tag = readEntityTag(golem);
        require(tag.getBooleanOr(LEGACY_PROBE_MARKER, false), "Legacy Copper Golem marker was lost");
        require(tag.getIntOr("deadrecall_data_version", 0) == 2, "Legacy data version was not loaded");
        require(tag.getIntOr("deadrecall_revision", 0) == 77, "Legacy revision was not loaded");
        require("gathering".equals(tag.getStringOr("deadrecall_mode", "")), "Legacy gathering mode was not loaded");
        require(!tag.getBooleanOr("deadrecall_transport_enabled", true), "Legacy stopped transport state was not loaded");
        require("returning_home".equals(tag.getStringOr("deadrecall_activity", "")), "Legacy activity was not loaded");
        require("minecraft:overworld".equals(tag.getStringOr("deadrecall_source_copper_container_dim", ""))
                        && tag.getIntOr("deadrecall_source_copper_container_x", Integer.MIN_VALUE) == HOME_POS.getX()
                        && tag.getIntOr("deadrecall_source_copper_container_y", Integer.MIN_VALUE) == HOME_POS.getY()
                        && tag.getIntOr("deadrecall_source_copper_container_z", Integer.MIN_VALUE) == HOME_POS.getZ(),
                "Legacy source binding was not loaded");
        require("minecraft:overworld".equals(tag.getStringOr("deadrecall_gathering_area_dim", ""))
                        && tag.getIntOr("deadrecall_gathering_corner_a_x", Integer.MIN_VALUE) == TARGET_POS.getX()
                        && tag.getIntOr("deadrecall_gathering_corner_b_y", Integer.MIN_VALUE) == TARGET_POS.getY() + 2
                        && tag.getLongOr("deadrecall_gathering_scan_index", -1L) == 73L,
                "Legacy gathering area or cursor was not loaded");
        require(tag.getIntOr("deadrecall_gathering_target_x", Integer.MIN_VALUE) == TARGET_POS.getX()
                        && tag.getIntOr("deadrecall_gathering_target_y", Integer.MIN_VALUE) == TARGET_POS.getY()
                        && tag.getIntOr("deadrecall_gathering_target_z", Integer.MIN_VALUE) == TARGET_POS.getZ(),
                "Legacy gathering target was not loaded");

        ItemStack tool = readItemStack(tag, "deadrecall_gathering_tool_stack");
        require(tool.is(Items.IRON_PICKAXE) && tool.getDamageValue() == 9
                        && LEGACY_TOOL_NAME.equals(tool.get(DataComponents.CUSTOM_NAME)),
                "Legacy gathering tool was not loaded");
        ItemStack storage = readItemStack(tag, "deadrecall_gathering_storage_stack");
        require(storage.is(Items.COBBLESTONE) && storage.getCount() == 12
                        && LEGACY_STORAGE_NAME.equals(storage.get(DataComponents.CUSTOM_NAME)),
                "Legacy gathering storage was not loaded");
        ItemStack fuel = readItemStack(tag, "deadrecall_fuel_stack");
        require(fuel.is(Items.COAL) && fuel.getCount() == 2 && tag.getIntOr("deadrecall_fuel_ticks", 0) == 321,
                "Legacy fuel state was not loaded");
        require(homeContainer(level).getItem(0).is(Items.EMERALD)
                        && homeContainer(level).getItem(0).getCount() == 3,
                "Legacy Home inventory was not loaded");
        require(level.getBlockState(TARGET_POS).is(Blocks.STONE), "Legacy target block was not loaded");
    }

    private static void verifyMigratedState(ServerLevel level, Path legacyMarkerDirectory) {
        CopperGolem golem = requireGolem(level, readLegacyGolemId(legacyMarkerDirectory));
        CompoundTag tag = readEntityTag(golem);
        require(tag.getBooleanOr(LEGACY_PROBE_MARKER, false), "Migrated Copper Golem marker was lost");
        require(tag.getIntOr("deadrecall_data_version", 0) == 2, "Migrated data version was lost");
        require(tag.getIntOr("deadrecall_revision", 0) == 78, "External Automata revision did not persist");
        require("gathering".equals(tag.getStringOr("deadrecall_mode", "")), "Migrated gathering mode was lost");
        require(!tag.getBooleanOr("deadrecall_transport_enabled", true), "Migrated stopped transport state was lost");
        require("blocked_home_unavailable".equals(tag.getStringOr("deadrecall_activity", "")),
                "External Automata activity did not persist");
        require(tag.getLongOr("deadrecall_gathering_scan_index", -1L) == 144L
                        && tag.getIntOr("deadrecall_gathering_target_y", Integer.MIN_VALUE) == TARGET_POS.getY() + 1,
                "External Automata gathering state did not persist");

        ItemStack tool = readItemStack(tag, "deadrecall_gathering_tool_stack");
        require(tool.is(Items.IRON_PICKAXE) && tool.getDamageValue() == 23
                        && LEGACY_TOOL_NAME.equals(tool.get(DataComponents.CUSTOM_NAME)),
                "External Automata tool mutation did not persist");
        ItemStack storage = readItemStack(tag, "deadrecall_gathering_storage_stack");
        require(storage.is(Items.COBBLESTONE) && storage.getCount() == 7
                        && MIGRATED_STORAGE_NAME.equals(storage.get(DataComponents.CUSTOM_NAME)),
                "External Automata storage mutation did not persist");
        ItemStack fuel = readItemStack(tag, "deadrecall_fuel_stack");
        require(fuel.is(Items.COAL) && fuel.getCount() == 2 && tag.getIntOr("deadrecall_fuel_ticks", 0) == 111,
                "External Automata fuel mutation did not persist");
        require(homeContainer(level).getItem(0).is(Items.DIAMOND)
                        && homeContainer(level).getItem(0).getCount() == 5,
                "External Automata Home inventory mutation did not persist");
        require(level.getBlockState(TARGET_POS).is(Blocks.DEEPSLATE),
                "External Automata target block mutation did not persist");
        level.setChunkForced(CHUNK_X, CHUNK_Z, false);
    }

    private static void requireExternalAutomataAuthority() {
        String version = FabricLoader.getInstance().getModContainer("totem-automata")
                .orElseThrow(() -> new IllegalStateException("Legacy migration probe requires TotemAutomata"))
                .getMetadata().getVersion().getFriendlyString();
        require("0.1.1".equals(version), "Legacy migration probe requires pinned Automata 0.1.1, found " + version);
        require(AutomataCutover.usesExternalAuthority(), "DeadRecall retained its legacy Automata authority");
    }

    private static CopperGolem requireGolem(ServerLevel level, UUID golemId) {
        Entity entity = level.getEntity(golemId);
        if (entity instanceof CopperGolem golem) {
            return golem;
        }
        throw new IllegalStateException("Legacy Copper Golem did not reload: " + golemId);
    }

    private static Container homeContainer(ServerLevel level) {
        Object blockEntity = level.getBlockEntity(HOME_POS);
        if (blockEntity instanceof Container container) {
            return container;
        }
        throw new IllegalStateException("Legacy Copper Golem Home is not a container");
    }

    private static CompoundTag readEntityTag(CopperGolem golem) {
        CustomData data = golem.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    private static void writeEntityTag(CopperGolem golem, CompoundTag tag) {
        golem.setComponent(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static ItemStack readItemStack(CompoundTag tag, String key) {
        return tag.read(key, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY).copy();
    }

    private static void writeItemStack(CompoundTag tag, String key, ItemStack stack) {
        if (stack.isEmpty()) {
            tag.remove(key);
        } else {
            tag.store(key, ItemStack.OPTIONAL_CODEC, stack.copy());
        }
    }

    private static ItemStack namedStack(net.minecraft.world.item.Item item, int count, Component name) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(DataComponents.CUSTOM_NAME, name);
        return stack;
    }

    private static Path requiredDirectory(String environmentVariable) {
        String configured = System.getenv(environmentVariable);
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException("Missing " + environmentVariable);
        }
        return Path.of(configured).toAbsolutePath().normalize();
    }

    private static UUID readLegacyGolemId(Path legacyMarkerDirectory) {
        try {
            return UUID.fromString(Files.readString(legacyMarkerDirectory.resolve(LEGACY_UUID_FILE), StandardCharsets.UTF_8).trim());
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalStateException("Could not read legacy Copper Golem UUID", exception);
        }
    }

    private static void writeMarker(Path markerDirectory, String fileName, String content) {
        try {
            Files.createDirectories(markerDirectory);
            Files.writeString(markerDirectory.resolve(fileName), content, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write legacy Automata migration marker", exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static final class Session {
        private final String phase;
        private final Path legacyMarkerDirectory;
        private final Path probeMarkerDirectory;
        private int ticks = 120;
        private boolean executed;

        private Session(String phase, Path legacyMarkerDirectory, Path probeMarkerDirectory) {
            this.phase = phase;
            this.legacyMarkerDirectory = legacyMarkerDirectory;
            this.probeMarkerDirectory = probeMarkerDirectory;
        }

        private void tick(MinecraftServer server) {
            if (--ticks > 0) {
                return;
            }
            try {
                if (!executed) {
                    runPhase(server, phase, legacyMarkerDirectory);
                    executed = true;
                    ticks = 80;
                    return;
                }
                writeMarker(probeMarkerDirectory, phase + ".ok", "success\n");
                server.halt(false);
            } catch (Throwable throwable) {
                writeMarker(probeMarkerDirectory, phase + ".failure", throwable + "\n");
                server.halt(false);
                throw new IllegalStateException("Legacy Automata migration probe failed in phase " + phase, throwable);
            }
        }
    }
}
