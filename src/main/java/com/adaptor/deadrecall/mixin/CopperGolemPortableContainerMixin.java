package com.adaptor.deadrecall.mixin;

import com.adaptor.deadrecall.inventory.PortableContainerPolicy;
import com.adaptor.deadrecall.item.copper.CopperGolemWrenchHandler;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Copper Golem sorting and gathering deposit simulate and perform insertion directly against the
 * target Container. Preserve the target's normal slot rules while also applying the portable
 * container policy to Shulker Box destinations.
 */
@Mixin(CopperGolemWrenchHandler.class)
public abstract class CopperGolemPortableContainerMixin {
    @Redirect(
            method = {
                    "hasMatchingDestinationItem",
                    "simulateInsert",
                    "mergeIntoSlot",
                    "placeIntoEmptySlot"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/Container;canPlaceItem(ILnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static boolean deadrecall$enforcePortableContainerPolicy(
            Container container,
            int slot,
            ItemStack stack
    ) {
        return PortableContainerPolicy.mayInsertIntoContainer(container, stack)
                && container.canPlaceItem(slot, stack);
    }
}
