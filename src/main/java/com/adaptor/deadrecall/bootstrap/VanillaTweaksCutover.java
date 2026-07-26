package com.adaptor.deadrecall.bootstrap;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

/** Selects TotemVanillaTweaks as the single owner of general vanilla Mixins. */
public final class VanillaTweaksCutover {
    private static final Version FIRST_CUTOVER_VERSION = firstCutoverVersion();

    private VanillaTweaksCutover() {
    }

    public static boolean usesExternalAuthority() {
        return FabricLoader.getInstance().getModContainer("totem-vanilla-tweaks")
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
