package com.adaptor.deadrecall.gametest;

import com.adaptor.deadrecall.inventory.BackpackInventory;
import com.adaptor.deadrecall.item.BackpackItemHelper;
import com.adaptor.deadrecall.item.ModItems;
import com.adaptor.deadrecall.mixin.DeadRecallSpaceUnitSavedDataAccessor;
import com.adaptor.deadrecall.space.DeadRecallSpaceDiscoverySavedData;
import com.adaptor.deadrecall.space.DeadRecallSpaceUnitSavedData;
import com.adaptor.deadrecall.space.SpaceUnitHandler;
import com.adaptor.deadrecall.space.SpaceUnitRecord;
import com.adaptor.deadrecall.space.SpaceUnitStatus;
import com.adaptor.deadrecall.space.SpaceUnitType;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.AABB;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-only integration probe invoked by CI across separate runGameTest JVMs.
 *
 * <p>The probe is part of the gametest source set and is never packaged in the production mod.
 * Enable one phase with {@code DEADRECALL_RESTART_PROBE_PHASE=seed|recover|verify}.</p>
 */
public final class DeathBackpackRestartProbe implements ModInitializer {
    private static final String PHASE_ENV = "DEADRECALL_RESTART_PROBE_PHASE";
    private static final UUID OWNER_ID = UUID.fromString("6b2fac01-f28d-43fd-b729-5aca6521bb56");
    private static final BlockPos PROBE_POS = new BlockPos(1000, 96, 1000);
    private static final Path MARKER_DIRECTORY = Path.of("restart-probe");
    private static final String BACKPACK_ID_TAG = "deadrecall_death_backpack_id";

    @Override
    public void onInitialize() {
        String phase = System.getenv(PHASE_ENV);
        if (phase == null || phase.isBlank()) {
            return;
        }

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                runPhase(server, phase);
                writeMarker(phase + ".ok", "success\n");
            } catch (Throwable throwable) {
                try {
                    writeMarker(phase + ".failure", throwable.toString() + "\n");
                } catch (RuntimeException markerFailure) {
                    throwable.addSuppressed(markerFailure);
                }
                throw new IllegalStateException("Death-backpack restart probe failed in phase " + phase, throwable);
            }
        });
    }

    private static void runPhase(MinecraftServer server, String phase) {
        ServerLevel level = server.overworld();
        level.getChunk(PROBE_POS);
        switch (phase) {
            case "seed" -> seed(level);
            case "recover" -> recover(server, level);
            case "verify" -> verify(level);
            default -> throw new IllegalArgumentException("Unknown restart probe phase: " + phase);
        }
    }

    private static void seed(ServerLevel level) {
        require(findProbeNode(level) == null, "Seed phase found a stale probe death node");
        require(findProbeBackpack(level) == null, "Seed phase found a stale probe death backpack");

        ServerPlayer owner = detachedPlayer(level.getServer(), level, OWNER_ID);
        DeadRecallSpaceUnitSavedData units = units(level);
        DeadRecallSpaceDiscoverySavedData discovery = discovery(level);
        SpaceUnitRecord node = units.createDeathUnit(level, PROBE_POS, owner);
        discovery.markDiscovered(OWNER_ID, node.id());

        ItemStack deathBackpack = new ItemStack(ModItems.DEATH_BACKPACK);
        CompoundTag customData = new CompoundTag();
        customData.putString(BACKPACK_ID_TAG, UUID.randomUUID().toString());
        deathBackpack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
        deathBackpack.set(
                DataComponents.CONTAINER,
                ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND, 11)))
        );
        SpaceUnitHandler.writeDeathNodeBinding(deathBackpack, node.id());

        ItemEntity entity = new ItemEntity(
                level,
                PROBE_POS.getX() + 0.5,
                PROBE_POS.getY() + 0.5,
                PROBE_POS.getZ() + 0.5,
                deathBackpack
        );
        entity.setNoGravity(true);
        entity.setUnlimitedLifetime();
        require(level.addFreshEntity(entity), "Seed phase could not add the probe death backpack entity");
        require(node.status() == SpaceUnitStatus.ACTIVE, "Seed phase did not create an ACTIVE death node");
    }

    private static void recover(MinecraftServer server, ServerLevel level) {
        SpaceUnitRecord node = requireProbeNode(level, SpaceUnitStatus.ACTIVE);
        ItemEntity entity = requireProbeBackpack(level);
        require(storedItems(entity.getItem()).stream().anyMatch(stack ->
                        stack.is(Items.DIAMOND) && stack.getCount() == 11),
                "Recover phase loaded a probe backpack with incorrect contents");

        ItemStack backpackStack = entity.getItem();
        entity.discard();
        ServerPlayer replacementOwner = detachedPlayer(server, level, OWNER_ID);
        replacementOwner.setItemInHand(InteractionHand.MAIN_HAND, backpackStack);
        BackpackInventory backpack = new BackpackInventory(replacementOwner, InteractionHand.MAIN_HAND, 9);
        backpack.clearContent();
        backpack.onClose(replacementOwner);

        require(replacementOwner.getMainHandItem().isEmpty(),
                "Recover phase left the empty death backpack on the replacement player");
        SpaceUnitRecord recovered = units(level).get(node.id())
                .orElseThrow(() -> new IllegalStateException("Recover phase lost the probe death node"));
        require(recovered.status() == SpaceUnitStatus.DISABLED,
                "Recover phase did not persistently disable the probe death node");
        require(discovery(level).hasDiscovered(OWNER_ID, node.id()),
                "Recover phase lost the probe discovery reference");
    }

    private static void verify(ServerLevel level) {
        SpaceUnitRecord node = requireProbeNode(level, SpaceUnitStatus.DISABLED);
        require(node.owner().equals(OWNER_ID), "Verify phase loaded the probe node with a different owner");
        require(node.type() == SpaceUnitType.DEATH, "Verify phase loaded the probe node with a different type");
        require(discovery(level).hasDiscovered(OWNER_ID, node.id()),
                "Verify phase did not reload the probe discovery reference");
        require(findProbeBackpack(level) == null,
                "Verify phase reloaded a death backpack that was removed before the previous shutdown");
    }

    private static SpaceUnitRecord requireProbeNode(ServerLevel level, SpaceUnitStatus expectedStatus) {
        SpaceUnitRecord node = findProbeNode(level);
        if (node == null) {
            throw new IllegalStateException("No probe death node was loaded");
        }
        require(node.status() == expectedStatus,
                "Probe death node status was " + node.status() + ", expected " + expectedStatus);
        return node;
    }

    private static SpaceUnitRecord findProbeNode(ServerLevel level) {
        return unitRecords(level).values().stream()
                .filter(unit -> unit.type() == SpaceUnitType.DEATH)
                .filter(unit -> unit.owner().equals(OWNER_ID))
                .findFirst()
                .orElse(null);
    }

    private static ItemEntity requireProbeBackpack(ServerLevel level) {
        ItemEntity entity = findProbeBackpack(level);
        if (entity == null) {
            throw new IllegalStateException("No probe death backpack entity was loaded");
        }
        return entity;
    }

    private static ItemEntity findProbeBackpack(ServerLevel level) {
        return level.getEntitiesOfClass(
                        ItemEntity.class,
                        new AABB(PROBE_POS).inflate(4.0),
                        entity -> entity.isAlive() && BackpackItemHelper.isDeathBackpackItem(entity.getItem())
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static List<ItemStack> storedItems(ItemStack backpack) {
        return backpack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                .nonEmptyItemCopyStream()
                .toList();
    }

    private static ServerPlayer detachedPlayer(
            MinecraftServer server,
            ServerLevel level,
            UUID playerId
    ) {
        return new ServerPlayer(
                server,
                level,
                new GameProfile(playerId, "restart-probe-owner"),
                ClientInformation.createDefault()
        );
    }

    private static DeadRecallSpaceUnitSavedData units(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(DeadRecallSpaceUnitSavedData.TYPE);
    }

    private static DeadRecallSpaceDiscoverySavedData discovery(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(DeadRecallSpaceDiscoverySavedData.TYPE);
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, SpaceUnitRecord> unitRecords(ServerLevel level) {
        return ((DeadRecallSpaceUnitSavedDataAccessor) (Object) units(level)).deadrecall$getUnitsById();
    }

    private static void writeMarker(String fileName, String content) {
        try {
            Files.createDirectories(MARKER_DIRECTORY);
            Files.writeString(
                    MARKER_DIRECTORY.resolve(fileName),
                    content,
                    StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write restart probe marker " + fileName, exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
