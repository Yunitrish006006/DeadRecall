package com.adaptor.deadrecall.bootstrap;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

/** Selects Remnant 0.1.4+ as owner of the remaining backpack integrations and client rendering. */
public final class RemnantCompletionCutover {
    private static final Version FIRST_EXTERNAL_VERSION = version("0.1.4");

    private RemnantCompletionCutover() {
    }

    public static boolean usesExternalAuthority() {
        return FabricLoader.getInstance().getModContainer("totem-remnant")
                .map(container -> container.getMetadata().getVersion()
                        .compareTo(FIRST_EXTERNAL_VERSION) >= 0)
                .orElse(false);
    }

    private static Version version(String value) {
        try {
            return Version.parse(value);
        } catch (VersionParsingException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
