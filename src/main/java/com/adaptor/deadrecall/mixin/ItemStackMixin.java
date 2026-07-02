package com.adaptor.deadrecall.mixin;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    /**
     * 讓原版書櫃物品在所有 feature 判定下都視為停用，
     * 以便從創造模式列表與一般物品可用性中移除。
     */
    @Inject(method = "isItemEnabled", at = @At("HEAD"), cancellable = true)
    private void deadrecall$disableVanillaBookshelf(FeatureFlagSet enabledFeatures, CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.is(Items.BOOKSHELF)) {
            cir.setReturnValue(false);
        }
    }
}
