package com.adaptor.deadrecall.item;

import com.adaptor.deadrecall.Deadrecall;
import com.adaptor.deadrecall.registry.TotemAutomataItemRegistration;
import com.adaptor.deadrecall.registry.TotemRemnantItemRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ModItems {
    public static final Item BACKPACK_BASIC = TotemRemnantItemRegistration.BACKPACK_BASIC;
    public static final Item BACKPACK_STANDARD = TotemRemnantItemRegistration.BACKPACK_STANDARD;
    public static final Item BACKPACK_ADVANCED = TotemRemnantItemRegistration.BACKPACK_ADVANCED;
    public static final Item BACKPACK_NETHERITE = TotemRemnantItemRegistration.BACKPACK_NETHERITE;
    public static final Item DEATH_BACKPACK = TotemRemnantItemRegistration.DEATH_BACKPACK;

    public static final Item SALTPETER = alchemyItem("saltpeter");
    public static final Item PIG_MANURE = alchemyItem("pig_manure");
    public static final Item WOOD_ASH = alchemyItem("wood_ash");
    public static final Item COCOA_POWDER = alchemyItem("cocoa_powder");
    public static final Item HOT_COCOA = alchemyItem("hot_cocoa");
    public static final Item CHERRY_BREW = alchemyItem("cherry_brew");
    public static final Item STONE_BOWL = alchemyItem("stone_bowl");
    public static final Item SULFUR_BOWL = alchemyItem("sulfur_bowl");

    public static final Item COPPER_WRENCH = TotemAutomataItemRegistration.COPPER_WRENCH;

    public static void registerModItems() {
        Deadrecall.LOGGER.info("正在註冊模組物品...");
    }

    private static Item alchemyItem(String path) {
        return BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("deadrecall", path))
                .map(reference -> reference.value())
                .orElse(Items.AIR);
    }
}
