package com.adaptor.deadrecall.gametest;

import com.adaptor.deadrecall.item.ModItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public final class PortableContainerNestingGameTest {
    private static final BlockPos SHULKER_POS = new BlockPos(2, 1, 2);
    private static final BlockPos HOPPER_POS = SHULKER_POS.above();

    @GameTest(maxTicks = 20)
    public void shulkerMenuAndSidedAutomationRejectBackpacks(GameTestHelper helper) {
        helper.setBlock(SHULKER_POS, Blocks.SHULKER_BOX);
        ShulkerBoxBlockEntity shulker = shulker(helper);
        ShulkerBoxSlot slot = new ShulkerBoxSlot(shulker, 0, 0, 0);

        ItemStack normalBackpack = new ItemStack(ModItems.BACKPACK_BASIC);
        ItemStack deathBackpack = new ItemStack(ModItems.DEATH_BACKPACK);

        require(helper, !slot.mayPlace(normalBackpack), "Shulker menu accepted a normal backpack");
        require(helper, !slot.mayPlace(deathBackpack), "Shulker menu accepted a death backpack");
        require(helper, !shulker.canPlaceItemThroughFace(0, normalBackpack, Direction.UP),
                "Sided automation accepted a normal backpack");
        require(helper, !shulker.canPlaceItemThroughFace(0, deathBackpack, Direction.UP),
                "Sided automation accepted a death backpack");
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void hopperCannotPushBackpackIntoShulker(GameTestHelper helper) {
        placeHopperOverShulker(helper);
        HopperBlockEntity hopper = hopper(helper);
        ShulkerBoxBlockEntity shulker = shulker(helper);
        hopper.setItem(0, new ItemStack(ModItems.BACKPACK_NETHERITE));

        helper.runAtTickTime(20, () -> {
            require(helper, hopper.getItem(0).is(ModItems.BACKPACK_NETHERITE),
                    "Hopper removed the backpack while targeting a Shulker Box");
            require(helper, isEmpty(shulker), "Hopper nested the backpack inside a Shulker Box");
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 40)
    public void hopperStillPushesOrdinaryItemsIntoShulker(GameTestHelper helper) {
        placeHopperOverShulker(helper);
        HopperBlockEntity hopper = hopper(helper);
        ShulkerBoxBlockEntity shulker = shulker(helper);
        hopper.setItem(0, new ItemStack(Items.DIRT, 3));

        helper.runAtTickTime(20, () -> {
            require(helper, hopper.getItem(0).isEmpty(), "Control hopper did not move ordinary items");
            require(helper, shulker.getItem(0).is(Items.DIRT) && shulker.getItem(0).getCount() == 3,
                    "Control Shulker Box did not receive ordinary items");
            helper.succeed();
        });
    }

    private static void placeHopperOverShulker(GameTestHelper helper) {
        helper.setBlock(SHULKER_POS, Blocks.SHULKER_BOX);
        helper.setBlock(
                HOPPER_POS,
                Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN)
        );
    }

    private static HopperBlockEntity hopper(GameTestHelper helper) {
        Object blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(HOPPER_POS));
        if (blockEntity instanceof HopperBlockEntity hopper) {
            return hopper;
        }
        throw helper.assertionException("Missing HopperBlockEntity fixture");
    }

    private static ShulkerBoxBlockEntity shulker(GameTestHelper helper) {
        Object blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(SHULKER_POS));
        if (blockEntity instanceof ShulkerBoxBlockEntity shulker) {
            return shulker;
        }
        throw helper.assertionException("Missing ShulkerBoxBlockEntity fixture");
    }

    private static boolean isEmpty(Container container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (!container.getItem(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            throw helper.assertionException(message);
        }
    }
}
