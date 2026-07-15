package com.adaptor.deadrecall.mixin;

import com.adaptor.deadrecall.space.DeadRecallFriendSavedData;
import com.adaptor.deadrecall.space.FriendTeleportSessionPolicy;
import com.adaptor.deadrecall.space.SpaceUnitHandler;
import com.adaptor.deadrecall.space.SpaceUnitStructureRefresh;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mixin(SpaceUnitHandler.class)
public abstract class SpaceUnitHandlerRefreshMixin {
    @Accessor("teleportSessions")
    public static Map<UUID, Object> deadrecall$getTeleportSessions() {
        throw new AssertionError();
    }

    @Accessor("pendingPlayerTeleportConsents")
    public static Map<?, ?> deadrecall$getLegacyPlayerTeleportConsents() {
        throw new AssertionError();
    }

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

    @Inject(method = "register", at = @At("TAIL"))
    private static void deadrecall$clearLegacyPlayerTeleportConsents(CallbackInfo ci) {
        deadrecall$getLegacyPlayerTeleportConsents().clear();
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
        deadrecall$getLegacyPlayerTeleportConsents().clear();

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
        if (deadrecall$getTeleportSessions().containsKey(player.getUUID())) {
            targetPlayer.sendSystemMessage(Component.empty()
                    .append(Component.translatable("message.deadrecall.space_unit.teleport_start"))
                    .append(Component.literal(": "))
                    .append(player.getDisplayName())
                    .append(Component.literal(" → "))
                    .append(targetPlayer.getDisplayName()));
        }
        ci.cancel();
    }

    @Inject(
            method = "removeFriend",
            at = @At("TAIL")
    )
    private static void deadrecall$cancelRemovedFriendTeleports(
            ServerPlayer player,
            UUID friendId,
            CallbackInfo ci
    ) {
        UUID playerId = player.getUUID();
        MinecraftServer server = player.level().getServer();
        Iterator<Map.Entry<UUID, Object>> iterator = deadrecall$getTeleportSessions().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Object> entry = iterator.next();
            if (!(entry.getValue() instanceof SpaceUnitTeleportSessionAccessor session)) {
                continue;
            }
            if (!FriendTeleportSessionPolicy.belongsToRelationship(
                    entry.getKey(),
                    session.deadrecall$getTargetUnitId(),
                    playerId,
                    friendId)) {
                continue;
            }

            iterator.remove();
            ServerPlayer requester = server.getPlayerList().getPlayer(entry.getKey());
            if (requester != null) {
                requester.sendSystemMessage(Component.translatable(
                        "message.deadrecall.space_unit.teleport_cancelled.target"));
            }
        }
    }
}
