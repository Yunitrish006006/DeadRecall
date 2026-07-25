package com.adaptor.deadrecall.network.registration;

import com.adaptor.deadrecall.bootstrap.AutomataCutover;
import com.adaptor.deadrecall.bootstrap.NexusCutover;
import net.fabricmc.loader.api.FabricLoader;

public final class DeadRecallPayloadRegistration {
    private DeadRecallPayloadRegistration() {
    }

    public static void register() {
        registerTypes();
        registerReceivers();
    }

    private static void registerTypes() {
        // Keep this sequence aligned with the legacy monolithic initializer.
        if (!usesExternalDiscordBridge()) {
            TotemDiscordBridgePayloadRegistration.registerServerboundTypes();
        }
        LegacyContainerPayloadRegistration.registerServerboundTypes();
        if (!AutomataCutover.usesExternalAuthority()) {
            TotemAutomataPayloadRegistration.registerServerboundTypes();
        }
        if (!NexusCutover.usesExternalAuthority()) {
            TotemNexusPayloadRegistration.registerServerboundTypes();
        }

        if (!usesExternalDiscordBridge()) {
            TotemDiscordBridgePayloadRegistration.registerClientboundTypes();
        }
        if (!AutomataCutover.usesExternalAuthority()) {
            TotemAutomataPayloadRegistration.registerClientboundTypes();
        }
        if (!NexusCutover.usesExternalAuthority()) {
            TotemNexusPayloadRegistration.registerClientboundTypes();
        }
    }

    private static void registerReceivers() {
        // Receiver order is also part of the compatibility-preserving extraction.
        if (!usesExternalDiscordBridge()) {
            TotemDiscordBridgePayloadRegistration.registerReceivers();
        }
        LegacyContainerPayloadRegistration.registerReceivers();
        if (!AutomataCutover.usesExternalAuthority()) {
            TotemAutomataPayloadRegistration.registerReceivers();
        }
        if (!NexusCutover.usesExternalAuthority()) {
            TotemNexusPayloadRegistration.registerReceivers();
        }
    }

    private static boolean usesExternalDiscordBridge() {
        return FabricLoader.getInstance().isModLoaded("totem-discord-bridge");
    }
}
