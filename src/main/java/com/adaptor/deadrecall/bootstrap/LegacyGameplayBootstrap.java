package com.adaptor.deadrecall.bootstrap;

import com.adaptor.deadrecall.alchemy.AlchemyHandler;
import com.adaptor.deadrecall.alchemy.CherryBrewInteractions;
import com.adaptor.deadrecall.alchemy.PigManureInteractions;

/**
 * Owns legacy gameplay registration until each remaining feature has an assigned module.
 */
public final class LegacyGameplayBootstrap {
    private LegacyGameplayBootstrap() {
    }

    public static void registerInteractions() {
        if (!AlchemyCutover.usesExternalAuthority()) {
            AlchemyHandler.register();
            CherryBrewInteractions.register();
            PigManureInteractions.register();
        }
    }
}
