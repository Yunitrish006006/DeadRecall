package com.adaptor.deadrecall.mixin;

import com.adaptor.deadrecall.item.DeathBackpackItem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Shadow
    public abstract ItemStack getStack();

    /**
     * 在 ItemEntity.damage() 最開頭注入，
     * 如果這個 ItemEntity 持有的物品是死亡背包，直接回傳 false（免疫所有傷害）。
     * 這個保護是基於物品類型而非實體類別，所以即使區塊重載後實體變回 vanilla ItemEntity 也有效。
     */
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void protectDeathBackpack(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.getStack().getItem() instanceof DeathBackpackItem) {
            cir.setReturnValue(false);
        }
    }
}

