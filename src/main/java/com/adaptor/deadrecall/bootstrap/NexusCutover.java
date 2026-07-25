package com.adaptor.deadrecall.bootstrap;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

/**
 * Selects the complete external Nexus authority only for the first immutable
 * cutover artifact. Earlier additive Nexus jars remain rollback-compatible.
 */
public final class NexusCutover {
    private static final Version FIRST_CUTOVER_VERSION = firstCutoverVersion();

    private NexusCutover() {
    }

    public static boolean usesExternalAuthority() {
        return FabricLoader.getInstance().getModContainer("totem-nexus")
                .map(container -> container.getMetadata().getVersion().compareTo(FIRST_CUTOVER_VERSION) >= 0)
                .orElse(false);
    }

    private static Version firstCutoverVersion() {
        try {
            return Version.parse("0.1.1");
        } catch (VersionParsingException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
