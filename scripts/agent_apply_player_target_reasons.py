from pathlib import Path

HANDLER = Path("src/main/java/com/adaptor/deadrecall/space/SpaceUnitHandler.java")
text = HANDLER.read_text(encoding="utf-8")


def replace_once(old: str, new: str, label: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one match, found {count}")
    text = text.replace(old, new, 1)


replace_once(
    """                target.get().id(),
                player.level().dimension(),
""",
    """                target.get().id(),
                target.get().type(),
                player.level().dimension(),
""",
    "session constructor",
)

replace_once(
    """            Optional<TeleportTarget> target = resolveTeleportTarget(player, session.targetUnitId(), false);
            if (target.isEmpty()) {
                iterator.remove();
                notify(player, Component.translatable("message.deadrecall.space_unit.teleport_cancelled.target"));
                continue;
            }
""",
    """            Optional<TeleportTarget> target = resolveTeleportTarget(player, session.targetUnitId(), false);
            if (target.isEmpty()) {
                iterator.remove();
                notify(player, targetCancelReason(player, session.targetType(), session.targetUnitId()));
                continue;
            }
""",
    "tick target cancellation",
)

replace_once(
    """        Optional<TeleportTarget> finalTarget = resolveTeleportTarget(player, target.id(), false, true);
        if (finalTarget.isEmpty()) {
            notify(player, Component.translatable("message.deadrecall.space_unit.teleport_cancelled.target"));
            return;
        }
""",
    """        Optional<TeleportTarget> finalTarget = resolveTeleportTarget(player, target.id(), false, true);
        if (finalTarget.isEmpty()) {
            notify(player, targetCancelReason(player, target.type(), target.id()));
            return;
        }
""",
    "completion target cancellation",
)

replace_once(
    """        if (targetUnit.isEmpty()) {
            ServerPlayer targetPlayer = server.getPlayerList().getPlayer(targetUnitId);
            if (targetPlayer == null || targetPlayer.getUUID().equals(player.getUUID())) {
                notifyIfRequested(player, notifyFailure, Component.translatable("message.deadrecall.space_unit.teleport_cancelled.target"));
                return Optional.empty();
            }
            if (!friends(server).areFriends(player.getUUID(), targetPlayer.getUUID())) {
                notifyIfRequested(player, notifyFailure, Component.translatable("message.deadrecall.space_unit.no_permission"));
                return Optional.empty();
            }
            return Optional.of(TeleportTarget.player(targetPlayer));
        }
""",
    """        if (targetUnit.isEmpty()) {
            ServerPlayer targetPlayer = server.getPlayerList().getPlayer(targetUnitId);
            if (targetPlayer == null) {
                notifyIfRequested(player, notifyFailure, Component.translatable(
                        PlayerTeleportTargetPolicy.cancellationMessageKey(PlayerTeleportTargetPolicy.State.OFFLINE)));
                return Optional.empty();
            }
            if (targetPlayer.getUUID().equals(player.getUUID())) {
                notifyIfRequested(player, notifyFailure, Component.translatable("message.deadrecall.space_unit.teleport_cancelled.target"));
                return Optional.empty();
            }

            PlayerTeleportTargetPolicy.State targetState = PlayerTeleportTargetPolicy.classify(
                    true,
                    targetPlayer.isAlive(),
                    targetPlayer.isRemoved(),
                    friends(server).areFriends(player.getUUID(), targetPlayer.getUUID())
            );
            if (targetState != PlayerTeleportTargetPolicy.State.AVAILABLE) {
                notifyIfRequested(player, notifyFailure, Component.translatable(
                        PlayerTeleportTargetPolicy.cancellationMessageKey(targetState)));
                return Optional.empty();
            }
            return Optional.of(TeleportTarget.player(targetPlayer));
        }
""",
    "player target resolution",
)

replace_once(
    """    private static Component teleportCancelReason(ServerPlayer player, TeleportSession session) {
""",
    """    private static Component targetCancelReason(
            ServerPlayer player,
            SpaceUnitType targetType,
            UUID targetId) {
        if (targetType != SpaceUnitType.PLAYER) {
            return Component.translatable("message.deadrecall.space_unit.teleport_cancelled.target");
        }

        MinecraftServer server = player.level().getServer();
        ServerPlayer targetPlayer = server.getPlayerList().getPlayer(targetId);
        PlayerTeleportTargetPolicy.State targetState = PlayerTeleportTargetPolicy.classify(
                targetPlayer != null,
                targetPlayer != null && targetPlayer.isAlive(),
                targetPlayer != null && targetPlayer.isRemoved(),
                targetPlayer != null && friends(server).areFriends(player.getUUID(), targetId)
        );
        return Component.translatable(PlayerTeleportTargetPolicy.cancellationMessageKey(targetState));
    }

    private static Component teleportCancelReason(ServerPlayer player, TeleportSession session) {
""",
    "target cancellation helper",
)

replace_once(
    """    private record TeleportSession(
            UUID playerId,
            String sourceType,
            UUID sourceUnitId,
            UUID targetUnitId,
            net.minecraft.resources.ResourceKey<Level> startDimension,
""",
    """    private record TeleportSession(
            UUID playerId,
            String sourceType,
            UUID sourceUnitId,
            UUID targetUnitId,
            SpaceUnitType targetType,
            net.minecraft.resources.ResourceKey<Level> startDimension,
""",
    "session target type field",
)

replace_once(
    """                    this.sourceUnitId,
                    this.targetUnitId,
                    this.startDimension,
""",
    """                    this.sourceUnitId,
                    this.targetUnitId,
                    this.targetType,
                    this.startDimension,
""",
    "session tick target type",
)

HANDLER.write_text(text, encoding="utf-8")

translations = {
    "src/main/resources/assets/deadrecall/lang/en_us.json": (
        '  "message.deadrecall.space_unit.teleport_cancelled.target": "Teleport cancelled: destination is no longer valid",\n',
        '  "message.deadrecall.space_unit.teleport_cancelled.target": "Teleport cancelled: destination is no longer valid",\n'
        '  "message.deadrecall.space_unit.teleport_cancelled.target_offline": "Teleport cancelled: player destination went offline",\n'
        '  "message.deadrecall.space_unit.teleport_cancelled.target_unavailable": "Teleport cancelled: player destination is dead or unavailable",\n'
        '  "message.deadrecall.space_unit.teleport_cancelled.target_friendship": "Teleport cancelled: the Space Unit friendship no longer exists",\n',
    ),
    "src/main/resources/assets/deadrecall/lang/zh_tw.json": (
        '  "message.deadrecall.space_unit.teleport_cancelled.target": "傳送取消：目的地已失效",\n',
        '  "message.deadrecall.space_unit.teleport_cancelled.target": "傳送取消：目的地已失效",\n'
        '  "message.deadrecall.space_unit.teleport_cancelled.target_offline": "傳送取消：玩家目的地已離線",\n'
        '  "message.deadrecall.space_unit.teleport_cancelled.target_unavailable": "傳送取消：玩家目的地已死亡或不可用",\n'
        '  "message.deadrecall.space_unit.teleport_cancelled.target_friendship": "傳送取消：Space Unit 好友關係已解除",\n',
    ),
    "src/main/resources/assets/deadrecall/lang/zh_cn.json": (
        '  "message.deadrecall.space_unit.teleport_cancelled.target": "传送取消：目的地已失效",\n',
        '  "message.deadrecall.space_unit.teleport_cancelled.target": "传送取消：目的地已失效",\n'
        '  "message.deadrecall.space_unit.teleport_cancelled.target_offline": "传送取消：玩家目的地已离线",\n'
        '  "message.deadrecall.space_unit.teleport_cancelled.target_unavailable": "传送取消：玩家目的地已死亡或不可用",\n'
        '  "message.deadrecall.space_unit.teleport_cancelled.target_friendship": "传送取消：Space Unit 好友关系已解除",\n',
    ),
}

for filename, (old, new) in translations.items():
    path = Path(filename)
    value = path.read_text(encoding="utf-8")
    if value.count(old) != 1:
        raise SystemExit(f"{filename}: generic target translation mismatch")
    path.write_text(value.replace(old, new, 1), encoding="utf-8")
