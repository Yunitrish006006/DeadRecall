package com.adaptor.deadrecall.inventory;

import com.adaptor.deadrecall.item.DeathBackpackItem;
import com.adaptor.deadrecall.item.TieredBackpackItem;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
        return List.of(
                Items.SHULKER_BOX,
                Items.WHITE_SHULKER_BOX,
                Items.ORANGE_SHULKER_BOX,
                Items.MAGENTA_SHULKER_BOX,
                Items.LIGHT_BLUE_SHULKER_BOX,
                Items.YELLOW_SHULKER_BOX,
                Items.LIME_SHULKER_BOX,
                Items.PINK_SHULKER_BOX,
                Items.GRAY_SHULKER_BOX,
                Items.LIGHT_GRAY_SHULKER_BOX,
                Items.CYAN_SHULKER_BOX,
                Items.PURPLE_SHULKER_BOX,
                Items.BLUE_SHULKER_BOX,
                Items.BROWN_SHULKER_BOX,
                Items.GREEN_SHULKER_BOX,
                Items.RED_SHULKER_BOX,
                Items.BLACK_SHULKER_BOX
        );
    }
}
