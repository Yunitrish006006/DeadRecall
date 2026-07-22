package com.adaptor.deadrecall.bootstrap;

import com.adaptor.deadrecall.registry.DeadRecallRegistryBootstrap;
import com.adaptor.deadrecall.death.RemnantNotificationBridge;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * Composes the server-side feature bootstraps in the legacy registration order.
 */
public final class DeadRecallServerBootstrap {
    private DeadRecallServerBootstrap() {
    }

    public static void register(Path configDir) {
        DeadRecallRegistryBootstrap.registerContent();
        LegacyGameplayBootstrap.registerInteractions();
        TotemAutomataBootstrap.register();
        TotemNexusBootstrap.register();
        LegacyGameplayBootstrap.registerRecipes();
        if (!FabricLoader.getInstance().isModLoaded("totem-discord-bridge")) {
            TotemDiscordBridgeBootstrap.register(configDir);
            TotemDiscordBridgeBootstrap.registerRuntime();
        }
        RemnantNotificationBridge.installIfPresent();
    }
}
