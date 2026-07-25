package com.adaptor.deadrecall.client;

import com.adaptor.deadrecall.bootstrap.AutomataCutover;
import com.adaptor.deadrecall.bootstrap.NexusCutover;
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
        if (!AutomataCutover.usesExternalAuthority()) {
            TotemAutomataClientBootstrap.registerScreens();
        }

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
        if (!AutomataCutover.usesExternalAuthority()) {
            TotemAutomataClientBootstrap.registerNetworking();
        }
        if (!NexusCutover.usesExternalAuthority()) {
            TotemNexusClientBootstrap.registerNetworking();
        }
        if (!usesExternalDiscordBridge()) {
            TotemDiscordBridgeClientBootstrap.registerCommands();
        }
    }

    private static boolean usesExternalDiscordBridge() {
        return FabricLoader.getInstance().isModLoaded("totem-discord-bridge");
    }
}
