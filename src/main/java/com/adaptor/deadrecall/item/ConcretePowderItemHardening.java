package com.adaptor.deadrecall.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;

public final class ConcretePowderItemHardening {
    private static final Map<Item, Item> HARDENED_ITEMS = Map.ofEntries(
            Map.entry(Blocks.WHITE_CONCRETE_POWDER.asItem(), Blocks.WHITE_CONCRETE.asItem()),
            Map.entry(Blocks.LIGHT_GRAY_CONCRETE_POWDER.asItem(), Blocks.LIGHT_GRAY_CONCRETE.asItem()),
            Map.entry(Blocks.GRAY_CONCRETE_POWDER.asItem(), Blocks.GRAY_CONCRETE.asItem()),
            Map.entry(Blocks.BLACK_CONCRETE_POWDER.asItem(), Blocks.BLACK_CONCRETE.asItem()),
            Map.entry(Blocks.BROWN_CONCRETE_POWDER.asItem(), Blocks.BROWN_CONCRETE.asItem()),
            Map.entry(Blocks.RED_CONCRETE_POWDER.asItem(), Blocks.RED_CONCRETE.asItem()),
            Map.entry(Blocks.ORANGE_CONCRETE_POWDER.asItem(), Blocks.ORANGE_CONCRETE.asItem()),
            Map.entry(Blocks.YELLOW_CONCRETE_POWDER.asItem(), Blocks.YELLOW_CONCRETE.asItem()),
            Map.entry(Blocks.LIME_CONCRETE_POWDER.asItem(), Blocks.LIME_CONCRETE.asItem()),
            Map.entry(Blocks.GREEN_CONCRETE_POWDER.asItem(), Blocks.GREEN_CONCRETE.asItem()),
            Map.entry(Blocks.CYAN_CONCRETE_POWDER.asItem(), Blocks.CYAN_CONCRETE.asItem()),
            Map.entry(Blocks.LIGHT_BLUE_CONCRETE_POWDER.asItem(), Blocks.LIGHT_BLUE_CONCRETE.asItem()),
            Map.entry(Blocks.BLUE_CONCRETE_POWDER.asItem(), Blocks.BLUE_CONCRETE.asItem()),
            Map.entry(Blocks.PURPLE_CONCRETE_POWDER.asItem(), Blocks.PURPLE_CONCRETE.asItem()),
            Map.entry(Blocks.MAGENTA_CONCRETE_POWDER.asItem(), Blocks.MAGENTA_CONCRETE.asItem()),
            Map.entry(Blocks.PINK_CONCRETE_POWDER.asItem(), Blocks.PINK_CONCRETE.asItem())
    );

    private ConcretePowderItemHardening() {
    }

    public static Item hardenedItem(Item powderItem) {
        return HARDENED_ITEMS.get(powderItem);
    }

    public static ItemStack harden(ItemStack stack) {
        if (stack.isEmpty()) {
            return stack;
        }

        Item hardenedItem = hardenedItem(stack.getItem());
        if (hardenedItem == null) {
            return stack;
        }
        return stack.transmuteCopy(hardenedItem, stack.getCount());
    }

    public static boolean tryHarden(ItemEntity itemEntity) {
        if (!(itemEntity.level() instanceof ServerLevel) || !itemEntity.isInWater()) {
            return false;
        }

        ItemStack current = itemEntity.getItem();
        ItemStack hardened = harden(current);
        if (hardened == current) {
            return false;
        }

        itemEntity.setItem(hardened);
        return true;
    }
}
