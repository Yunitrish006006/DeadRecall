package com.adaptor.deadrecall.bootstrap;

import com.adaptor.deadrecall.space.DistributedSpawnHandler;
import com.adaptor.deadrecall.space.SpaceUnitHandler;
import com.adaptor.deadrecall.core.api.DeathBackpackNodeLifecycle;
import com.adaptor.deadrecall.space.NexusDeathBackpackNodeAdapter;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Owns registration for the future TotemNexus module.
 */
public final class TotemNexusBootstrap {
    private TotemNexusBootstrap() {
    }

    public static void register() {
        DeathBackpackNodeLifecycle.register(new NexusDeathBackpackNodeAdapter());
        DistributedSpawnHandler.register();
        SpaceUnitHandler.register();

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, damageSource, baseDamageTaken, damageTaken, blocked) -> {
            if (entity instanceof ServerPlayer player && damageTaken > 0.0F) {
                SpaceUnitHandler.cancelTeleport(player, Component.translatable("message.deadrecall.space_unit.teleport_cancelled.damage"));
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(SpaceUnitHandler::tickTeleportSessions);
        ServerTickEvents.END_SERVER_TICK.register(SpaceUnitHandler::tickLodestoneIntegrity);
    }
}
