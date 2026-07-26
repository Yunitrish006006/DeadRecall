package com.adaptor.deadrecall.bootstrap;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

/**
 * Selects TotemAlchemy as the single authority for the preserved alchemy
 * surface once the first cutover-capable artifact is installed.
 */
public final class AlchemyCutover {
    private static final Version FIRST_CUTOVER_VERSION = firstCutoverVersion();

    private AlchemyCutover() {
    }

    public static boolean usesExternalAuthority() {
        return FabricLoader.getInstance().getModContainer("totem-alchemy")
                .map(container -> container.getMetadata().getVersion().compareTo(FIRST_CUTOVER_VERSION) >= 0)
                .orElse(false);
    }

    private static Version firstCutoverVersion() {
        try {
            return Version.parse("0.1.0");
        } catch (VersionParsingException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
