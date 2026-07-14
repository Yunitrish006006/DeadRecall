package com.adaptor.deadrecall.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * Vanilla chest menu with one additional invariant: the ItemStack backing the
 * currently open backpack cannot be moved while the menu is open.
 */
public final class BackpackMenu extends ChestMenu {
    private final Inventory playerInventory;
    private final BackpackInventory backpackInventory;

    public BackpackMenu(
            MenuType<?> menuType,
            int containerId,
            Inventory playerInventory,
            Container container,
            int rows
    ) {
        super(menuType, containerId, playerInventory, container, rows);
        this.playerInventory = playerInventory;
        this.backpackInventory = (BackpackInventory) container;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (isOpenedBackpackSlot(slotIndex)) {
            return ItemStack.EMPTY;
        }
        return super.quickMoveStack(player, slotIndex);
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType clickType, Player player) {
        if (isOpenedBackpackSlot(slotIndex) || swapsWithOpenedBackpack(button, clickType)) {
            return;
        }
        super.clicked(slotIndex, button, clickType, player);
    }

    private boolean isOpenedBackpackSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return false;
        }
        return this.backpackInventory.isTrackedStackReference(this.slots.get(slotIndex).getItem());
    }

    private boolean swapsWithOpenedBackpack(int button, ClickType clickType) {
        if (clickType != ClickType.SWAP) {
            return false;
        }

        if (button >= 0 && button < 9) {
            return this.backpackInventory.isTrackedStackReference(this.playerInventory.getItem(button));
        }

        return button == Inventory.SLOT_OFFHAND
                && this.backpackInventory.isTrackedStackReference(
                this.playerInventory.getItem(Inventory.SLOT_OFFHAND)
        );
    }
}
