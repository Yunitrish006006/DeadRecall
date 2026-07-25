package com.adaptor.deadrecall.bootstrap;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

/**
 * Selects the external Automata authority only for the first cutover-capable
 * artifact. Earlier additive Automata jars remain compatibility-only.
 */
public final class AutomataCutover {
    private static final Version FIRST_CUTOVER_VERSION = firstCutoverVersion();

    private AutomataCutover() {
    }

    public static boolean usesExternalAuthority() {
        return FabricLoader.getInstance().getModContainer("totem-automata")
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
