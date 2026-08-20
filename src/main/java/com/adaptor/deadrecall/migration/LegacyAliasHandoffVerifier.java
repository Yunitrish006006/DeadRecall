package com.adaptor.deadrecall.migration;

import dev.totem.core.api.v1.migration.LegacyItemMigrationRegistry;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/** Verifies that TotemCore owns the complete legacy DeadRecall alias table. */
public final class LegacyAliasHandoffVerifier {
    private static final Map<Identifier, Identifier> EXPECTED = expectedMappings();

    private LegacyAliasHandoffVerifier() {
    }

    public static void verify() {
        Map<Identifier, Identifier> actual = LegacyItemMigrationRegistry.snapshot();
        for (Map.Entry<Identifier, Identifier> expected : EXPECTED.entrySet()) {
            Identifier actualTarget = actual.get(expected.getKey());
            if (!expected.getValue().equals(actualTarget)) {
                throw new IllegalStateException(
                        "TotemCore legacy alias handoff is incomplete for " + expected.getKey()
                                + ": expected " + expected.getValue() + ", got " + actualTarget
                );
            }
        }
    }

    public static int mappingCount() {
        return EXPECTED.size();
    }

    private static Map<Identifier, Identifier> expectedMappings() {
        Map<Identifier, Identifier> mappings = new LinkedHashMap<>();
        put(mappings, "backpack_basic", "remnant/backpack_basic");
        put(mappings, "backpack_standard", "remnant/backpack_standard");
        put(mappings, "backpack_advanced", "remnant/backpack_advanced");
        put(mappings, "backpack_netherite", "remnant/backpack_netherite");
        put(mappings, "death_backpack", "remnant/death_backpack");
        put(mappings, "copper_wrench", "automata/copper_wrench");
        put(mappings, "saltpeter", "alchemy/saltpeter");
        put(mappings, "pig_manure", "alchemy/pig_manure");
        put(mappings, "wood_ash", "alchemy/wood_ash");
        put(mappings, "cocoa_powder", "alchemy/cocoa_powder");
        put(mappings, "hot_cocoa", "alchemy/hot_cocoa");
        put(mappings, "cherry_brew", "alchemy/cherry_brew");
        put(mappings, "stone_bowl", "alchemy/stone_bowl");
        put(mappings, "sulfur_bowl", "alchemy/sulfur_bowl");
        return Map.copyOf(mappings);
    }

    private static void put(Map<Identifier, Identifier> mappings, String legacyPath, String canonicalPath) {
        mappings.put(
                Identifier.fromNamespaceAndPath("deadrecall", legacyPath),
                Identifier.fromNamespaceAndPath("totem", canonicalPath)
        );
    }
}
