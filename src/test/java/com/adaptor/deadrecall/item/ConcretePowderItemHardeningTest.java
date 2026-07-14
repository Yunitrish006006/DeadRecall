package com.adaptor.deadrecall.item;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcretePowderItemHardeningTest {
    @BeforeAll
    static void bootStrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Bootstrap.validate();
    }

    @Test
    void mapsAllVanillaConcretePowderColors() {
        Map<Item, Item> expected = Map.ofEntries(
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

        expected.forEach((powder, concrete) -> assertSame(concrete, ConcretePowderItemHardening.hardenedItem(powder)));
    }

    @Test
    void preservesCountAndCompatibleComponents() {
        ItemStack powder = new ItemStack(Items.CYAN_CONCRETE_POWDER, 37);
        Component customName = Component.literal("Construction batch");
        powder.set(DataComponents.CUSTOM_NAME, customName);

        ItemStack hardened = ConcretePowderItemHardening.harden(powder);

        assertTrue(hardened.is(Items.CYAN_CONCRETE));
        assertEquals(37, hardened.getCount());
        assertEquals(customName, hardened.get(DataComponents.CUSTOM_NAME));
    }

    @Test
    void leavesUnsupportedItemsUntouched() {
        ItemStack stone = new ItemStack(Items.STONE, 5);

        assertSame(stone, ConcretePowderItemHardening.harden(stone));
    }
}
