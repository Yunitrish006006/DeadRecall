package com.adaptor.deadrecall.gametest;

import com.adaptor.deadrecall.bootstrap.NexusCutover;
import com.adaptor.deadrecall.mixin.DeadRecallSpaceUnitSavedDataAccessor;
import com.adaptor.deadrecall.space.DeadRecallDistributedSpawnSavedData;
import com.adaptor.deadrecall.space.DeadRecallFriendSavedData;
import com.adaptor.deadrecall.space.DeadRecallSpaceDiscoverySavedData;
import com.adaptor.deadrecall.space.DeadRecallSpaceUnitSavedData;
import com.adaptor.deadrecall.space.SpaceStructureSnapshot;
import com.adaptor.deadrecall.space.SpaceUnitRecord;
import com.adaptor.deadrecall.space.SpaceUnitStatus;
import com.adaptor.deadrecall.space.SpaceUnitType;
import com.adaptor.deadrecall.space.SpaceUnitVisibility;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Persists the legacy root Nexus SavedData graph, then proves that the
 * external 0.1.1 authority loads and changes it across two Dedicated Server
 * JVM restarts without loading a root Nexus authority class.
 */
public final class LegacyNexusMigrationProbe implements ModInitializer {
    private static final String PHASE_ENV = "DEADRECALL_LEGACY_NEXUS_MIGRATION_PHASE";
    private static final String MARKER_DIRECTORY_ENV = "DEADRECALL_LEGACY_NEXUS_PROBE_MARKER_DIR";
    private static final UUID OWNER = UUID.fromString("71a8a033-2494-4055-8de9-2b6dbb741000");
    private static final UUID FRIEND = UUID.fromString("0f8bca6d-6cb4-4ef0-b9b5-f1e5c3da1000");
    private static final UUID UNIT_ID = UUID.fromString("1f1e1100-6f6f-4a44-92ed-a061beefa001");
    private static final BlockPos ANCHOR_POS = new BlockPos(176, 96, 176);
    private static final String LEGACY_NAME = "Legacy Nexus Anchor";
    private static final String MIGRATED_NAME = "External Nexus Anchor";

    @Override
    public void onInitialize() {
        String phase = System.getenv(PHASE_ENV);
        if (phase == null || phase.isBlank()) {
            return;
        }
        Path markerDirectory = requiredDirectory();
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
                ServerTickEvents.END_SERVER_TICK.register(new Session(phase, markerDirectory)::tick));
    }

    private static void runPhase(MinecraftServer server, String phase) {
        switch (phase) {
            case "seed" -> seedLegacyRootState(server.overworld());
            case "migrate" -> migrateWithExternalAuthority(server);
            case "verify" -> verifyExternalRestartState(server);
            default -> throw new IllegalArgumentException("Unknown legacy Nexus migration phase: " + phase);
        }
    }

    private static void seedLegacyRootState(ServerLevel level) {
        require(!NexusCutover.usesExternalAuthority(), "Legacy Nexus seed unexpectedly selected external authority");
        require(level.setBlockAndUpdate(ANCHOR_POS, Blocks.LODESTONE.defaultBlockState()),
                "Could not place legacy Nexus lodestone anchor");

        MinecraftServer server = level.getServer();
        DeadRecallSpaceUnitSavedData units = server.overworld().getDataStorage()
                .computeIfAbsent(DeadRecallSpaceUnitSavedData.TYPE);
        Map<UUID, SpaceUnitRecord> records =
                ((DeadRecallSpaceUnitSavedDataAccessor) (Object) units).deadrecall$getUnitsById();
        require(!records.containsKey(UNIT_ID), "Legacy Nexus seed found stale Space Unit state");
        records.put(UNIT_ID, new SpaceUnitRecord(
                UNIT_ID,
                SpaceUnitType.LODESTONE,
                level.dimension(),
                ANCHOR_POS,
                OWNER,
                LEGACY_NAME,
                SpaceUnitVisibility.FRIENDS,
                SpaceUnitStatus.ACTIVE,
                Set.of(),
                Set.of(FRIEND),
                SpaceStructureSnapshot.EMPTY,
                level.getGameTime(),
                level.getGameTime()
        ));
        units.setDirty();

        DeadRecallSpaceDiscoverySavedData discovery = server.overworld().getDataStorage()
                .computeIfAbsent(DeadRecallSpaceDiscoverySavedData.TYPE);
        require(discovery.markDiscovered(OWNER, UNIT_ID), "Could not seed legacy Nexus discovery");
        require(discovery.markDiscovered(FRIEND, UNIT_ID), "Could not seed legacy Nexus friend discovery");
        require(discovery.setFavorite(OWNER, UNIT_ID, true), "Could not seed legacy Nexus favorite");

        DeadRecallFriendSavedData friends = server.overworld().getDataStorage()
                .computeIfAbsent(DeadRecallFriendSavedData.TYPE);
        require(friends.inviteOrAccept(OWNER, FRIEND) == DeadRecallFriendSavedData.FriendActionResult.INVITED,
                "Could not seed legacy Nexus friendship invite");
        require(friends.inviteOrAccept(FRIEND, OWNER) == DeadRecallFriendSavedData.FriendActionResult.ACCEPTED,
                "Could not seed legacy Nexus friendship");

        DeadRecallDistributedSpawnSavedData spawns = server.overworld().getDataStorage()
                .computeIfAbsent(DeadRecallDistributedSpawnSavedData.TYPE);
        spawns.put(OWNER, level.dimension(), ANCHOR_POS.above(), 37.5F, level.getGameTime());
    }

    private static void migrateWithExternalAuthority(MinecraftServer server) {
        requireExternalNexusAuthority();
        Object units = externalData(server, "dev.totem.nexus.space.NexusSpaceUnitSavedData");
        verifyExternalLegacyGraph(server, units, LEGACY_NAME, true);
        Object renameResult = invoke(units, "setLodestoneName",
                new Class<?>[]{UUID.class, UUID.class, String.class, long.class},
                UNIT_ID, OWNER, MIGRATED_NAME, server.overworld().getGameTime());
        require(optional(renameResult).isPresent(), "External Nexus rejected an owner rename of the legacy Space Unit");
        verifyExternalLegacyGraph(server, units, MIGRATED_NAME, true);
    }

    private static void verifyExternalRestartState(MinecraftServer server) {
        requireExternalNexusAuthority();
        Object units = externalData(server, "dev.totem.nexus.space.NexusSpaceUnitSavedData");
        verifyExternalLegacyGraph(server, units, MIGRATED_NAME, true);
    }

    private static void verifyExternalLegacyGraph(
            MinecraftServer server,
            Object units,
            String expectedName,
            boolean expectedFavorite) {
        Optional<?> unit = optional(invoke(units, "get", new Class<?>[]{UUID.class}, UNIT_ID));
        require(unit.isPresent(), "External Nexus lost legacy Space Unit");
        Object record = unit.get();
        Object actualName = invoke(record, "name", new Class<?>[0]);
        require(expectedName.equals(actualName),
                "External Nexus did not retain the legacy Space Unit name: expected "
                        + expectedName + ", found " + actualName);
        require(OWNER.equals(invoke(record, "owner", new Class<?>[0])),
                "External Nexus changed the legacy Space Unit owner");
        require("LODESTONE".equals(String.valueOf(invoke(record, "type", new Class<?>[0]))),
                "External Nexus changed the legacy Space Unit type");
        require("ACTIVE".equals(String.valueOf(invoke(record, "status", new Class<?>[0]))),
                "External Nexus changed the legacy Space Unit status");

        Object discovery = externalData(server, "dev.totem.nexus.space.NexusSpaceDiscoverySavedData");
        require(Boolean.TRUE.equals(invoke(discovery, "hasDiscovered", new Class<?>[]{UUID.class, UUID.class}, OWNER, UNIT_ID)),
                "External Nexus lost owner discovery");
        require(Boolean.valueOf(expectedFavorite).equals(invoke(
                discovery, "isFavorite", new Class<?>[]{UUID.class, UUID.class}, OWNER, UNIT_ID)),
                "External Nexus changed the legacy favorite state");

        Object friends = externalData(server, "dev.totem.nexus.space.NexusFriendSavedData");
        require(Boolean.TRUE.equals(invoke(friends, "areFriends", new Class<?>[]{UUID.class, UUID.class}, OWNER, FRIEND)),
                "External Nexus lost legacy friendship state");

        Object spawns = externalData(server, "dev.totem.nexus.space.NexusDistributedSpawnSavedData");
        Optional<?> spawn = optional(invoke(spawns, "get", new Class<?>[]{UUID.class}, OWNER));
        require(spawn.isPresent(), "External Nexus lost legacy distributed spawn");
        require(ANCHOR_POS.above().equals(invoke(spawn.get(), "pos", new Class<?>[0])),
                "External Nexus changed the legacy distributed spawn position");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object externalData(MinecraftServer server, String className) {
        try {
            Class<?> dataClass = Class.forName(className);
            SavedDataType type = (SavedDataType) dataClass.getField("TYPE").get(null);
            return server.overworld().getDataStorage().computeIfAbsent(type);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not load external Nexus SavedData " + className, exception);
        }
    }

    private static Object invoke(Object target, String method, Class<?>[] parameterTypes, Object... arguments) {
        try {
            Method reflected = target.getClass().getMethod(method, parameterTypes);
            return reflected.invoke(target, arguments);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Could not invoke external Nexus method " + method, exception);
        }
    }

    private static Optional<?> optional(Object value) {
        if (value instanceof Optional<?> optional) {
            return optional;
        }
        throw new IllegalStateException("External Nexus returned a non-optional value");
    }

    private static void requireExternalNexusAuthority() {
        String version = FabricLoader.getInstance().getModContainer("totem-nexus")
                .orElseThrow(() -> new IllegalStateException("Legacy Nexus migration probe requires TotemNexus"))
                .getMetadata().getVersion().getFriendlyString();
        require("0.1.4".equals(version), "Legacy Nexus migration probe requires pinned Nexus 0.1.4, found " + version);
        require(NexusCutover.usesExternalAuthority(), "DeadRecall retained its legacy Nexus authority");
    }

    private static Path requiredDirectory() {
        String configured = System.getenv(MARKER_DIRECTORY_ENV);
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException("Missing " + MARKER_DIRECTORY_ENV);
        }
        return Path.of(configured).toAbsolutePath().normalize();
    }

    private static void writeMarker(Path directory, String name, String content) {
        try {
            Files.createDirectories(directory);
            Files.writeString(directory.resolve(name), content, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write legacy Nexus migration marker", exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static final class Session {
        private final String phase;
        private final Path markerDirectory;
        private int ticks = 120;
        private boolean executed;

        private Session(String phase, Path markerDirectory) {
            this.phase = phase;
            this.markerDirectory = markerDirectory;
        }

        private void tick(MinecraftServer server) {
            if (--ticks > 0) {
                return;
            }
            try {
                if (!executed) {
                    runPhase(server, phase);
                    executed = true;
                    ticks = 80;
                    return;
                }
                writeMarker(markerDirectory, phase + ".ok", "success\n");
                server.halt(false);
            } catch (Throwable throwable) {
                writeMarker(markerDirectory, phase + ".failure", throwable + "\n");
                server.halt(false);
                throw new IllegalStateException("Legacy Nexus migration probe failed in phase " + phase, throwable);
            }
        }
    }
}
