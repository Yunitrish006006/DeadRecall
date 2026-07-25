package com.adaptor.deadrecall.registry;

import com.adaptor.deadrecall.advancement.ModCriteriaTriggers;
import com.adaptor.deadrecall.bootstrap.AutomataCutover;
import com.adaptor.deadrecall.block.ModBlocks;
import com.adaptor.deadrecall.block.entity.ModBlockEntities;
import com.adaptor.deadrecall.effect.ModMobEffects;
import com.adaptor.deadrecall.item.ModItemGroups;
import com.adaptor.deadrecall.menu.ModMenus;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Composes registry owners in the legacy all-in-one registration order.
 */
public final class DeadRecallRegistryBootstrap {
    private DeadRecallRegistryBootstrap() {
    }

    public static void registerContent() {
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerModBlockEntities();
        ModMobEffects.registerModEffects();

        boolean externalAutomata = AutomataCutover.usesExternalAuthority();
        LegacyGameplayCriteriaRegistration.register();
        if (!externalAutomata) {
            TotemAutomataCriteriaRegistration.register();
        }
        ModCriteriaTriggers.registerModCriteriaTriggers();

        if (!externalAutomata) {
            TotemAutomataMenuRegistration.register();
        }
        ModMenus.registerModMenus();

        if (!FabricLoader.getInstance().isModLoaded("totem-remnant")) {
            TotemRemnantItemRegistration.register();
        }
        LegacyGameplayItemRegistration.register();
        if (!externalAutomata) {
            TotemAutomataItemRegistration.register();
        }
        DeadRecallItemRegistrationLog.register();

        ModItemGroups.registerModItemGroups();
    }
}
