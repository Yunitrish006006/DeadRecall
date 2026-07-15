package com.adaptor.deadrecall.gametest;

import com.adaptor.deadrecall.item.BackpackItemHelper;
import com.adaptor.deadrecall.item.ModItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class DeathBackpackCaptureGameTest {
    private static final BlockPos DEATH_POS = new BlockPos(2, 2, 2);

    @SuppressWarnings("removal")
    @GameTest(maxTicks = 60)
    public void capturesInventoryBeforeVanillaCreatesWorldDrops(GameTestHelper helper) {
        helper.setBlock(DEATH_POS.below(), Blocks.STONE);
        BlockPos absoluteDeathPos = helper.absolutePos(DEATH_POS);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.snapTo(
                absoluteDeathPos.getX() + 0.5,
                absoluteDeathPos.getY(),
                absoluteDeathPos.getZ() + 0.5,
                0.0F,
                0.0F
        );
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 12));
        player.getInventory().setItem(1, new ItemStack(ModItems.BACKPACK_BASIC, 1));

        player.die(helper.getLevel().damageSources().generic());

        helper.runAtTickTime(5, () -> {
            try {
                AABB searchBox = new AABB(absoluteDeathPos).inflate(4.0);
                List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(
                        ItemEntity.class,
                        searchBox,
                        ItemEntity::isAlive
                );

                List<ItemEntity> deathBackpacks = drops.stream()
                        .filter(entity -> BackpackItemHelper.isDeathBackpackItem(entity.getItem()))
                        .toList();
                require(helper, deathBackpacks.size() == 1,
                        "Expected exactly one death backpack, found " + deathBackpacks.size());

                ItemStack deathBackpack = deathBackpacks.getFirst().getItem();
                ItemContainerContents contents = deathBackpack.getOrDefault(
                        DataComponents.CONTAINER,
                        ItemContainerContents.EMPTY
                );
                List<ItemStack> stored = contents.nonEmptyItemCopyStream().toList();
                require(helper, stored.stream().anyMatch(stack -> stack.is(Items.DIAMOND) && stack.getCount() == 12),
                        "Death backpack did not contain the captured diamond stack");
                require(helper, stored.stream().noneMatch(BackpackItemHelper::isBackpackItem),
                        "A backpack was nested inside the death backpack");

                require(helper, drops.stream().noneMatch(entity -> entity.getItem().is(Items.DIAMOND)),
                        "Captured diamonds were still emitted as world ItemEntities");
                require(helper, drops.stream().anyMatch(entity -> entity.getItem().is(ModItems.BACKPACK_BASIC)),
                        "Excluded tiered backpack was not emitted by vanilla");
                require(helper, player.getInventory().getItem(0).isEmpty(),
                        "Captured inventory slot was not cleared");
                require(helper, player.getInventory().getItem(1).isEmpty(),
                        "Vanilla did not clear the excluded backpack slot after dropping it");

                helper.succeed();
            } finally {
                player.discard();
            }
        });
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            throw helper.assertionException(message);
        }
    }
}
