package com.adaptor.deadrecall.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

public final class ConcretePowderItemHardening {
    private static final Map<Item, Item> HARDENED_ITEMS = Map.ofEntries(
            Map.entry(Items.WHITE_CONCRETE_POWDER, Items.WHITE_CONCRETE),
            Map.entry(Items.LIGHT_GRAY_CONCRETE_POWDER, Items.LIGHT_GRAY_CONCRETE),
            Map.entry(Items.GRAY_CONCRETE_POWDER, Items.GRAY_CONCRETE),
            Map.entry(Items.BLACK_CONCRETE_POWDER, Items.BLACK_CONCRETE),
            Map.entry(Items.BROWN_CONCRETE_POWDER, Items.BROWN_CONCRETE),
            Map.entry(Items.RED_CONCRETE_POWDER, Items.RED_CONCRETE),
            Map.entry(Items.ORANGE_CONCRETE_POWDER, Items.ORANGE_CONCRETE),
            Map.entry(Items.YELLOW_CONCRETE_POWDER, Items.YELLOW_CONCRETE),
            Map.entry(Items.LIME_CONCRETE_POWDER, Items.LIME_CONCRETE),
            Map.entry(Items.GREEN_CONCRETE_POWDER, Items.GREEN_CONCRETE),
            Map.entry(Items.CYAN_CONCRETE_POWDER, Items.CYAN_CONCRETE),
            Map.entry(Items.LIGHT_BLUE_CONCRETE_POWDER, Items.LIGHT_BLUE_CONCRETE),
            Map.entry(Items.BLUE_CONCRETE_POWDER, Items.BLUE_CONCRETE),
            Map.entry(Items.PURPLE_CONCRETE_POWDER, Items.PURPLE_CONCRETE),
            Map.entry(Items.MAGENTA_CONCRETE_POWDER, Items.MAGENTA_CONCRETE),
            Map.entry(Items.PINK_CONCRETE_POWDER, Items.PINK_CONCRETE)
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
