package com.adaptor.deadrecall.inventory;

import com.adaptor.deadrecall.item.DeathBackpackItem;
import com.adaptor.deadrecall.item.TieredBackpackItem;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortableContainerPolicyTest {
    @BeforeAll
    static void bootStrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Bootstrap.validate();
    }

    @Test
    void everyDeadRecallBackpackRejectsVanillaContainerItems() {
        TieredBackpackItem tiered = new TieredBackpackItem(
                properties("tiered_backpack"),
                TieredBackpackItem.BackpackTier.BASIC
        );
        DeathBackpackItem death = new DeathBackpackItem(properties("death_backpack"));

        assertFalse(tiered.canFitInsideContainerItems());
        assertFalse(death.canFitInsideContainerItems());
        assertFalse(PortableContainerPolicy.mayInsertIntoPortableContainer(stack(tiered)));
        assertFalse(PortableContainerPolicy.mayInsertIntoPortableContainer(stack(death)));
    }

    @Test
    void bundleAndEveryShulkerColorAreRestrictedInsideBackpacks() {
        assertTrue(PortableContainerPolicy.isBundle(new ItemStack(Items.BUNDLE)));
        assertFalse(PortableContainerPolicy.mayInsertIntoBackpack(new ItemStack(Items.BUNDLE)));

        for (Item shulker : shulkerBoxes()) {
            ItemStack stack = new ItemStack(shulker);
            assertTrue(PortableContainerPolicy.isShulkerBox(stack));
            assertFalse(PortableContainerPolicy.mayInsertIntoBackpack(stack));
        }
    }

    @Test
    void ordinaryItemsRemainInsertable() {
        ItemStack dirt = new ItemStack(Items.DIRT);

        assertFalse(PortableContainerPolicy.isRestrictedPortableContainer(dirt));
        assertTrue(PortableContainerPolicy.mayInsertIntoBackpack(dirt));
        assertTrue(PortableContainerPolicy.mayInsertIntoPortableContainer(dirt));
    }

    private static Item.Properties properties(String path) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("deadrecall_test", path)
        );
        return new Item.Properties().setId(key).stacksTo(1);
    }

    private static ItemStack stack(Item item) {
        return new ItemStack(Holder.direct(item, DataComponentMap.builder()
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .build()), 1);
    }

    private static List<Item> shulkerBoxes() {
        return Arrays.stream(new String[]{
                        "shulker_box",
                        "white_shulker_box",
                        "orange_shulker_box",
                        "magenta_shulker_box",
                        "light_blue_shulker_box",
                        "yellow_shulker_box",
                        "lime_shulker_box",
                        "pink_shulker_box",
                        "gray_shulker_box",
                        "light_gray_shulker_box",
                        "cyan_shulker_box",
                        "purple_shulker_box",
                        "blue_shulker_box",
                        "brown_shulker_box",
                        "green_shulker_box",
                        "red_shulker_box",
                        "black_shulker_box"
                })
                .map(PortableContainerPolicyTest::vanillaItem)
                .toList();
    }

    private static Item vanillaItem(String path) {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", path));
        assertNotNull(item, "Missing vanilla item minecraft:" + path);
        return item;
    }
}
