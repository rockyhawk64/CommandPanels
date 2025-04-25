package me.rockyhawk.commandpanels.interaction.commands.paywalls.itempaywall;

import org.bukkit.inventory.ItemStack;

public class InventoryOperationResult {
    private final boolean success;
    private final ItemStack[] inventory;

    public InventoryOperationResult(boolean success, ItemStack[] inventory) {
        this.success = success;
        this.inventory = inventory;
    }

    public boolean isSuccess() {
        return success;
    }

    public ItemStack[] getInventory() {
        return inventory;
    }
}