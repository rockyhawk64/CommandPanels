package me.rockyhawk.commandpanels.interaction.commands.paywalls.itempaywall;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.items.CompareUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemRemovalHandler {

    public boolean removeItem(Context ctx, Player player, ItemStack itemToRemove, boolean ignoreNBT, boolean performOperation) {
        InventoryOperationResult result;

        if (ctx.inventorySaver.hasNormalInventory(player)) {
            result = removeItemFromInventory(ctx, player.getInventory().getContents(), itemToRemove, ignoreNBT, performOperation);
            player.getInventory().setContents(result.getInventory());
        } else {
            ItemStack[] savedInventory = ctx.inventorySaver.getNormalInventory(player);
            result = removeItemFromInventory(ctx, savedInventory, itemToRemove, ignoreNBT, performOperation);
            ctx.inventorySaver.inventoryConfig.set(player.getUniqueId().toString(), ctx.itemSerializer.itemStackArrayToBase64(result.getInventory()));
        }

        return result.isSuccess();
    }

    private InventoryOperationResult removeItemFromInventory(Context ctx, ItemStack[] inventory, ItemStack itemToRemove, boolean ignoreNBT, boolean performOperation) {
        CompareUtil identical = new CompareUtil(ctx);
        int amountToRemove = itemToRemove.getAmount();
        int count = 0;

        for (ItemStack item : inventory) {
            if (item != null && identical.isIdentical(item, itemToRemove, !ignoreNBT)) {
                count += item.getAmount();
            }
        }

        if (count < amountToRemove) {
            return new InventoryOperationResult(false, inventory);
        }

        if (!performOperation) {
            return new InventoryOperationResult(true, inventory);
        }

        for (int i = 0; i < inventory.length; i++) {
            ItemStack currentItem = inventory[i];
            if (currentItem != null && identical.isIdentical(currentItem, itemToRemove, !ignoreNBT)) {
                int removeAmount = Math.min(currentItem.getAmount(), amountToRemove);
                currentItem.setAmount(currentItem.getAmount() - removeAmount);
                amountToRemove -= removeAmount;

                if (currentItem.getAmount() == 0) {
                    inventory[i] = null;
                }

                if (amountToRemove == 0) {
                    break;
                }
            }
        }

        return new InventoryOperationResult(true, inventory);
    }
}
