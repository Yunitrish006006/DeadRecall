package com.adaptor.deadrecall.item;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
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

        expected.forEach((powder, concrete) -> assertSame(concrete, ConcretePowderItemHardening.hardenedItem(powder)));
    }

    @Test
    void preservesFullStackCountAndCompatibleComponents() {
        ItemStack powder = new ItemStack(Blocks.CYAN_CONCRETE_POWDER.asItem(), 64);
        Component customName = Component.literal("Construction batch");
        powder.set(DataComponents.CUSTOM_NAME, customName);

        ItemStack hardened = ConcretePowderItemHardening.harden(powder);

        assertTrue(hardened.is(Blocks.CYAN_CONCRETE.asItem()));
        assertEquals(64, hardened.getCount());
        assertEquals(customName, hardened.get(DataComponents.CUSTOM_NAME));
    }

    @Test
    void leavesUnsupportedItemsUntouched() {
        ItemStack stone = new ItemStack(Blocks.STONE.asItem(), 5);

        assertSame(stone, ConcretePowderItemHardening.harden(stone));
    }
}
