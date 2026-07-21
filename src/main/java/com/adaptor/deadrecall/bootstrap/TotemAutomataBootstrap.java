package com.adaptor.deadrecall.bootstrap;

import com.adaptor.deadrecall.item.copper.CopperGolemWrenchHandler;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/**
 * Owns registration for the future TotemAutomata module.
 */
public final class TotemAutomataBootstrap {
    private TotemAutomataBootstrap() {
    }

    public static void register() {
        CopperGolemWrenchHandler.register();

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof net.minecraft.world.entity.animal.golem.CopperGolem golem) {
                CopperGolemWrenchHandler.clearGatheringDisplayedItem(golem);
            }
            return true;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof net.minecraft.world.entity.animal.golem.CopperGolem golem) {
                CopperGolemWrenchHandler.dropGatheringInventory(golem);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(CopperGolemWrenchHandler::tickCopperGolemWrenchState);
    }
}
