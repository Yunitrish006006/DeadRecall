package com.adaptor.deadrecall.death;

import com.adaptor.deadrecall.Deadrecall;
import com.adaptor.deadrecall.DiscordBridge;
import com.adaptor.deadrecall.core.api.DeathBackpackNodeLifecycle;
import com.adaptor.deadrecall.core.api.DeathBackpackRecoveryTransport;
import com.adaptor.deadrecall.api.death.DeathBackpackNodeBinding;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Completes the lifecycle of a death Space Unit when its bound death backpack is emptied.
 *
 * <p>The backpack binding, rather than the identity of the player who empties it, identifies the
 * node to disable. Normal players cannot author arbitrary custom data, while allowing another
 * player to recover a backpack must not leave the original owner's node permanently active.</p>
 */
public final class DeathBackpackRecoveryService {
    private static final String TAG_DEATH_NODE_ID = "deadrecall_space_death_node_id";
    private static final Set<UUID> FORCED_NOTIFICATION_FAILURES = new HashSet<>();

    private DeathBackpackRecoveryService() {
    }

    public static boolean recoverBoundNode(ServerPlayer recoveringPlayer, ItemStack deathBackpack) {
        var externalTransport = DeathBackpackRecoveryTransport.current();
        if (externalTransport.isPresent() && externalTransport.get().recover(recoveringPlayer, deathBackpack)) {
            return true;
        }

        UUID unitId = DeathBackpackNodeBinding.read(deathBackpack);
        if (unitId == null) {
            return false;
        }

        boolean disabled = DeathBackpackNodeLifecycle.current()
                .map(adapter -> adapter.recover(recoveringPlayer, unitId))
                .orElse(false);
        if (!disabled) {
            return false;
        }

        notifyRecoveredSafely(recoveringPlayer);
        return true;
    }

    static void forceNotificationFailureForTesting(UUID playerId) {
        FORCED_NOTIFICATION_FAILURES.add(playerId);
    }

    static void clearForcedNotificationFailureForTesting(UUID playerId) {
        FORCED_NOTIFICATION_FAILURES.remove(playerId);
    }

    private static void notifyRecoveredSafely(ServerPlayer player) {
        try {
            if (FORCED_NOTIFICATION_FAILURES.remove(player.getUUID())) {
                throw new IllegalStateException("Forced death-backpack recovery notification failure");
            }
            player.sendSystemMessage(Component.translatable("message.deadrecall.space_unit.death_node_recovered"));
        } catch (RuntimeException exception) {
            Deadrecall.LOGGER.warn(
                    "Death node was recovered, but the player notification failed for {}",
                    player.getName().getString(),
                    exception
            );
        }

        try {
            DiscordBridge.sendDeathBackpackRecovered(player.getName().getString());
        } catch (RuntimeException exception) {
            Deadrecall.LOGGER.warn(
                    "Death node was recovered, but the Discord notification failed for {}",
                    player.getName().getString(),
                    exception
            );
        }
    }
}
