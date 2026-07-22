package com.adaptor.deadrecall.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Composes the client-side feature bootstraps in the legacy registration order.
 */
public final class DeadRecallClientBootstrap {
    private DeadRecallClientBootstrap() {
    }

    public static void register() {
        TotemAutomataClientBootstrap.registerScreens();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("deadrecall", "category")
        );
        if (!usesExternalDiscordBridge()) {
            DeadrecallClient.openDiscordConfigKey = TotemDiscordBridgeClientBootstrap.createKeyMapping(category);
        }
        DeadrecallClient.sortBackpackKey = LegacyContainerClientBootstrap.createKeyMapping(category);

        if (!usesExternalDiscordBridge()) {
            TotemDiscordBridgeClientBootstrap.registerRuntime();
        }
        TotemAutomataClientBootstrap.registerNetworking();
        TotemNexusClientBootstrap.registerNetworking();
        if (!usesExternalDiscordBridge()) {
            TotemDiscordBridgeClientBootstrap.registerCommands();
        }
    }

    private static boolean usesExternalDiscordBridge() {
        return FabricLoader.getInstance().isModLoaded("totem-discord-bridge");
    }
}
