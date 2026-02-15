package com.adaptor.deadrecall.item;

import com.adaptor.deadrecall.screen.BackpackScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class DeathBackpackItem extends Item {
    public DeathBackpackItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            // 在伺服器端開啟死亡背包介面
            user.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, player) ->
                    new BackpackScreenHandler(syncId, playerInventory, player, hand, TieredBackpackItem.BackpackTier.ADVANCED),
                Text.translatable("container.deadrecall.death_backpack")
            ));
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.literal("死亡背包 - 收集死亡掉落物品")
            .formatted(Formatting.RED));
        tooltip.add(Text.literal("容量: 27格 (3排)")
            .formatted(Formatting.GRAY));
        tooltip.add(Text.literal("防火保護")
            .formatted(Formatting.GOLD));
    }
}
