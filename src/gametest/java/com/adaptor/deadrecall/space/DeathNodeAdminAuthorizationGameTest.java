package com.adaptor.deadrecall.space;

import com.adaptor.deadrecall.network.RequestDeathNodeAdminPayload;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

/** Server-side authorization coverage for death-node administration payloads. */
public final class DeathNodeAdminAuthorizationGameTest {
    private static final BlockPos PLAYER_POS = new BlockPos(2, 2, 2);
    private static final BlockPos NODE_POS = new BlockPos(6, 2, 2);

    @SuppressWarnings("removal")
    @GameTest(maxTicks = 40)
    public void nonAdministratorCannotMutateADeathNodeWithAForgedUuid(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createPlayer(helper, PLAYER_POS);
        SpaceUnitRecord node = units(level).createDeathUnit(level, helper.absolutePos(NODE_POS), player);

        try {
            require(helper, !DeathNodeAdminService.canManage(player),
                    "GameTest mock player unexpectedly has death-node administration permission");

            // The node UUID is deliberately supplied by the client-facing action API.
            DeathNodeAdminService.handleAction(player, node.id(), DeathNodeAdminService.ACTION_DISABLE);

            SpaceUnitRecord current = units(level).get(node.id())
                    .orElseThrow(() -> helper.assertionException("Forged action removed the death node"));
            require(helper, current.status() == SpaceUnitStatus.ACTIVE,
                    "Non-administrator forged node UUID changed an active death node");
            helper.succeed();
        } finally {
            units(level).get(node.id()).ifPresent(ignored -> units(level).disableDeathUnit(
                    player.getUUID(), node.id(), level.getGameTime()));
            player.discard();
        }
    }

    @SuppressWarnings("removal")
    @GameTest(maxTicks = 40)
    public void nonAdministratorCannotReceiveASnapshotFromAForgedQuery(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createPlayer(helper, PLAYER_POS);
        SpaceUnitRecord node = units(level).createDeathUnit(level, helper.absolutePos(NODE_POS), player);

        try {
            boolean snapshotSent = DeathNodeAdminService.sendSnapshot(
                    player,
                    new RequestDeathNodeAdminPayload(player.getUUID().toString(), "", "", 0L, 0L, 0)
            );

            require(helper, !snapshotSent,
                    "Unauthorized query unexpectedly received a private death-node snapshot");
            SpaceUnitRecord current = units(level).get(node.id())
                    .orElseThrow(() -> helper.assertionException("Unauthorized query changed the death node"));
            require(helper, current.status() == SpaceUnitStatus.ACTIVE,
                    "Unauthorized query changed a death node while being denied");
            helper.succeed();
        } finally {
            units(level).get(node.id()).ifPresent(ignored -> units(level).disableDeathUnit(
                    player.getUUID(), node.id(), level.getGameTime()));
            player.discard();
        }
    }

    private static ServerPlayer createPlayer(GameTestHelper helper, BlockPos relativePos) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.getLevel().setBlockAndUpdate(absolutePos.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(absolutePos, Blocks.AIR.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(absolutePos.above(), Blocks.AIR.defaultBlockState());
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.snapTo(absolutePos.getX() + 0.5D, absolutePos.getY(), absolutePos.getZ() + 0.5D, 0.0F, 0.0F);
        return player;
    }

    private static DeadRecallSpaceUnitSavedData units(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(DeadRecallSpaceUnitSavedData.TYPE);
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            throw helper.assertionException(message);
        }
    }
}
