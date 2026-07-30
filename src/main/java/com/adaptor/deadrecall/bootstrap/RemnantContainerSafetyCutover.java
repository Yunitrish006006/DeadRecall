package com.adaptor.deadrecall.bootstrap;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

/** Selects Remnant 0.1.3+ as the single owner of portable-container safety. */
public final class RemnantContainerSafetyCutover {
    private static final Version FIRST_EXTERNAL_VERSION = version("0.1.3");

    private RemnantContainerSafetyCutover() {
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
