package com.adaptor.deadrecall.bootstrap;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

/** Selects TotemVanillaTweaks as the single owner of general vanilla Mixins. */
public final class VanillaTweaksCutover {
    private static final Version FIRST_CUTOVER_VERSION = firstCutoverVersion();
    private static final Version FIRST_CONTAINER_SORT_CUTOVER_VERSION = version("0.1.2");
    private static final Version FIRST_BOOKSHELF_CUTOVER_VERSION = version("0.1.3");

    private VanillaTweaksCutover() {
    }

    public static boolean usesExternalAuthority() {
        return FabricLoader.getInstance().getModContainer("totem-vanilla-tweaks")
                .map(container -> container.getMetadata().getVersion().compareTo(FIRST_CUTOVER_VERSION) >= 0)
                .orElse(false);
    }

    public static boolean usesExternalContainerSortAuthority() {
        return FabricLoader.getInstance().getModContainer("totem-vanilla-tweaks")
                .map(container -> container.getMetadata().getVersion()
                        .compareTo(FIRST_CONTAINER_SORT_CUTOVER_VERSION) >= 0)
                .orElse(false);
    }

    public static boolean usesExternalBookshelfAuthority() {
        return FabricLoader.getInstance().getModContainer("totem-vanilla-tweaks")
                .map(container -> container.getMetadata().getVersion()
                        .compareTo(FIRST_BOOKSHELF_CUTOVER_VERSION) >= 0)
                .orElse(false);
    }

    private static Version firstCutoverVersion() {
        return version("0.1.0");
    }

    private static Version version(String value) {
        try {
            return Version.parse(value);
        } catch (VersionParsingException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
