package com.adaptor.deadrecall.migration;

import dev.totem.core.api.v1.migration.LegacyItemMigrationRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.List;

/** Owns every item identifier retained solely for DeadRecall world migration. */
public final class DeadRecallLegacyItems {
    private static final String LEGACY_NAMESPACE = "deadrecall";
    private static final List<Alias> ALIASES = List.of(
            alias("backpack_basic", "totem", "remnant/backpack_basic", "BACKPACK_BASIC", 1, false, null),
            alias("backpack_standard", "totem", "remnant/backpack_standard", "BACKPACK_STANDARD", 1, false, null),
            alias("backpack_advanced", "totem", "remnant/backpack_advanced", "BACKPACK_ADVANCED", 1, false, null),
            alias("backpack_netherite", "totem", "remnant/backpack_netherite", "BACKPACK_NETHERITE", 1, true, null),
            alias("death_backpack", "totem", "remnant/death_backpack", "DEATH_BACKPACK", 1, true, null),
            alias("copper_wrench", "totem", "automata/copper_wrench", "COPPER_WRENCH", 1, false, null),
            alias("saltpeter", "totem", "alchemy/saltpeter", "SALTPETER", 64, false, null),
            alias("pig_manure", "totem", "alchemy/pig_manure", "PIG_MANURE", 64, false, null),
            alias("wood_ash", "totem", "alchemy/wood_ash", "WOOD_ASH", 64, false, null),
            alias("cocoa_powder", "totem", "alchemy/cocoa_powder", "COCOA_POWDER", 1, false, null),
            alias("hot_cocoa", "totem", "alchemy/hot_cocoa", "HOT_COCOA", 16, false, null),
            alias("cherry_brew", "totem", "alchemy/cherry_brew", "CHERRY_BREW", 16, false, null),
            alias("stone_bowl", "totem", "alchemy/stone_bowl", "STONE_BOWL", 1, false, null),
            alias(
                    "sulfur_bowl",
                    "totem",
                    "alchemy/sulfur_bowl",
                    "SULFUR_BOWL",
                    1,
                    false,
                    Identifier.fromNamespaceAndPath("totem", "alchemy/stone_bowl")
            )
    );

    private static boolean registered;

    private DeadRecallLegacyItems() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }

        for (Alias alias : ALIASES) {
            Item canonicalItem = resolveCanonicalItem(alias);
            LegacyItemMigrationRegistry.register(
                    alias.legacyId(),
                    alias.canonicalId(),
                    canonicalItem
            );
            registerLegacyItem(alias);
        }
        registered = true;
    }

    public static int mappingCount() {
        return ALIASES.size();
    }

    private static void registerLegacyItem(Alias alias) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, alias.legacyId());
        if (BuiltInRegistries.ITEM.containsKey(alias.legacyId())) {
            throw new IllegalStateException(
                    "Legacy item ID must be owned only by DeadRecall: " + alias.legacyId()
            );
        }

        Item.Properties properties = new Item.Properties()
                .setId(key)
                .stacksTo(alias.maxStackSize());
        if (alias.fireResistant()) {
            properties.fireResistant();
        }
        if (alias.craftRemainderId() != null) {
            properties.craftRemainder(ensureCanonicalItem(alias.craftRemainderId()));
        }

        Registry.register(
                BuiltInRegistries.ITEM,
                alias.legacyId(),
                new LegacyMigratingItem(properties)
        );
    }

    private static Item ensureCanonicalItem(Identifier id) {
        for (Alias alias : ALIASES) {
            if (alias.canonicalId().equals(id)) {
                return resolveCanonicalItem(alias);
            }
        }
        throw new IllegalStateException("Unknown canonical migration target: " + id);
    }

    private static Item resolveCanonicalItem(Alias alias) {
        try {
            Object value = Class.forName(alias.ownerClass())
                    .getField(alias.ownerField())
                    .get(null);
            if (!(value instanceof Item item)) {
                throw new IllegalStateException(
                        alias.ownerClass() + "." + alias.ownerField() + " is not an Item"
                );
            }
            Identifier actualId = BuiltInRegistries.ITEM.getKey(item);
            if (!alias.canonicalId().equals(actualId)) {
                throw new IllegalStateException(
                        "Canonical item field " + alias.ownerClass() + "." + alias.ownerField()
                                + " has ID " + actualId + ", expected " + alias.canonicalId()
                );
            }
            return item;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Missing canonical item field "
                            + alias.ownerClass() + "." + alias.ownerField(),
                    exception
            );
        }
    }

    private static Alias alias(
            String legacyPath,
            String canonicalNamespace,
            String canonicalPath,
            String ownerField,
            int maxStackSize,
            boolean fireResistant,
            Identifier craftRemainderId
    ) {
        String ownerClass;
        if (canonicalPath.startsWith("remnant/")) {
            ownerClass = "dev.totem.remnant.registry.RemnantItemRegistration";
        } else if (canonicalPath.startsWith("automata/")) {
            ownerClass = "dev.totem.automata.registry.AutomataRegistries";
        } else if (canonicalPath.startsWith("alchemy/")) {
            ownerClass = "dev.totem.alchemy.registry.AlchemyItems";
        } else {
            throw new IllegalArgumentException("Unknown canonical item owner: " + canonicalPath);
        }
        return new Alias(
                Identifier.fromNamespaceAndPath(LEGACY_NAMESPACE, legacyPath),
                Identifier.fromNamespaceAndPath(canonicalNamespace, canonicalPath),
                ownerClass,
                ownerField,
                maxStackSize,
                fireResistant,
                craftRemainderId
        );
    }

    private record Alias(
            Identifier legacyId,
            Identifier canonicalId,
            String ownerClass,
            String ownerField,
            int maxStackSize,
            boolean fireResistant,
            Identifier craftRemainderId
    ) {
    }
}
