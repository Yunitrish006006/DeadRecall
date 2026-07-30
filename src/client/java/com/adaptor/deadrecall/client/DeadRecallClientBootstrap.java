package com.adaptor.deadrecall.client;

import com.adaptor.deadrecall.bootstrap.AutomataCutover;
import com.adaptor.deadrecall.bootstrap.NexusCutover;
import com.adaptor.deadrecall.bootstrap.VanillaTweaksCutover;
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

        boolean externalDiscordBridge = usesExternalDiscordBridge();
        boolean externalContainerSort = VanillaTweaksCutover.usesExternalContainerSortAuthority();
        KeyMapping.Category legacyCategory = null;
        if (!externalDiscordBridge) {
            legacyCategory = KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath("deadrecall", "category")
            );
        }
        if (!externalDiscordBridge) {
            DeadrecallClient.openDiscordConfigKey =
                    TotemDiscordBridgeClientBootstrap.createKeyMapping(legacyCategory);
        }
        if (!externalContainerSort) {
            KeyMapping.Category sortCategory =
                    legacyCategory != null ? legacyCategory : KeyMapping.Category.INVENTORY;
            DeadrecallClient.sortBackpackKey =
                    LegacyContainerClientBootstrap.createKeyMapping(sortCategory);
        }

        if (!externalDiscordBridge) {
            TotemDiscordBridgeClientBootstrap.registerRuntime();
        }
        if (!AutomataCutover.usesExternalAuthority()) {
            TotemAutomataClientBootstrap.registerNetworking();
        }
        if (!NexusCutover.usesExternalAuthority()) {
            TotemNexusClientBootstrap.registerNetworking();
        }
        if (!externalDiscordBridge) {
            TotemDiscordBridgeClientBootstrap.registerCommands();
        }
    }

    private static boolean usesExternalDiscordBridge() {
        return FabricLoader.getInstance().isModLoaded("totem-discord-bridge");
    }
}
