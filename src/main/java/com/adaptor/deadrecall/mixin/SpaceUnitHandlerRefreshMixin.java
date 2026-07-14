package com.adaptor.deadrecall.mixin;

import com.adaptor.deadrecall.space.DeadRecallFriendSavedData;
import com.adaptor.deadrecall.space.SpaceUnitHandler;
import com.adaptor.deadrecall.space.SpaceUnitStructureRefresh;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(SpaceUnitHandler.class)
public abstract class SpaceUnitHandlerRefreshMixin {
    @Invoker("startTeleport")
    public static void deadrecall$startAuthorizedTeleport(
            ServerPlayer player,
            String sourceType,
            UUID sourceUnitId,
            UUID targetUnitId,
            boolean playerTargetConsentGranted
    ) {
        throw new AssertionError();
    }

    @Inject(
            method = "sendSpaceUnitMap(Lnet/minecraft/server/level/ServerPlayer;Ljava/util/UUID;)V",
            at = @At("HEAD")
    )
    private static void deadrecall$refreshMapSource(
            ServerPlayer player,
            UUID sourceUnitId,
            CallbackInfo ci
    ) {
        SpaceUnitStructureRefresh.refresh(player.level().getServer(), sourceUnitId);
    }

    @Inject(
            method = "startTeleport(Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/String;Ljava/util/UUID;Ljava/util/UUID;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void deadrecall$refreshTeleportRouteAndAuthorizeFriends(
            ServerPlayer player,
            String sourceType,
            UUID sourceUnitId,
            UUID targetUnitId,
            CallbackInfo ci
    ) {
        MinecraftServer server = player.level().getServer();
        if (SpaceUnitHandler.SOURCE_TYPE_LODESTONE.equals(sourceType)) {
            SpaceUnitStructureRefresh.refresh(server, sourceUnitId);
        }
        SpaceUnitStructureRefresh.refresh(server, targetUnitId);

        if (targetUnitId == null || targetUnitId.equals(player.getUUID())) {
            return;
        }

        ServerPlayer targetPlayer = server.getPlayerList().getPlayer(targetUnitId);
        if (targetPlayer == null || !targetPlayer.isAlive() || targetPlayer.isRemoved()) {
            return;
        }

        DeadRecallFriendSavedData friendData = server.overworld()
                .getDataStorage()
                .computeIfAbsent(DeadRecallFriendSavedData.TYPE);
        if (!friendData.areFriends(player.getUUID(), targetUnitId)) {
            return;
        }

        deadrecall$startAuthorizedTeleport(player, sourceType, sourceUnitId, targetUnitId, true);
        ci.cancel();
    }
}
