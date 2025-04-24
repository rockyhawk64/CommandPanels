package me.rockyhawk.commandpanels.commandtags.paywalls.itempaywall;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.commandtags.PaywallEvent;
import me.rockyhawk.commandpanels.commandtags.PaywallOutput;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemPaywall implements Listener {
    Context ctx;
    public ItemPaywall(Context pl) {
        this.ctx = pl;
    }

    @EventHandler
    public void commandTag(PaywallEvent e){
        if(e.name.equalsIgnoreCase("item-paywall=")){
            //if player uses item-paywall= [Material] [Amount] <IGNORENBT> WILL NOT TAKE CUSTOM ITEMS. IGNORENBT lets nbt items through. Useful for spawner edge cases.
            //player can use item-paywall= [custom-item] [Amount]
            try {
                boolean ignoreNBT = false;
                for (String val : e.args) {
                    //This is here for when people want to take nbt items like spawners with types in a check for spawners.
                    if (val.equals("IGNORENBT")) {
                        ignoreNBT = true;
                        break;
                    }
                }

                //create the item to be removed
                ItemStack sellItem;
                if (Material.matchMaterial(e.args[0]) == null) {
                    //If custom item set to custom item data.
                    sellItem = ctx.itemCreate.makeCustomItemFromConfig(e.panel, PanelPosition.Top, e.panel.getConfig().getConfigurationSection("custom-item." + e.args[0]), e.p, true, true, false);
                    sellItem.setAmount(Integer.parseInt(e.args[1]));
                } else {
                    //If normal just set material.
                    sellItem = new ItemStack(Objects.requireNonNull(Material.matchMaterial(e.args[0])), Integer.parseInt(e.args[1]));
                }

                //try to remove the item and determine outcome
                PaywallOutput removedItem = PaywallOutput.Blocked;
                if(removeItem(e.p, sellItem, ignoreNBT, e.doDelete)){
                    removedItem = PaywallOutput.Passed;
                }

                //send message and return
                if (removedItem == PaywallOutput.Blocked) {
                    if (ctx.configHandler.isTrue("purchase.item.enable")) {
                        //no item found to remove
                        ctx.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.item.failure")));
                    }
                } else {
                    if (ctx.configHandler.isTrue("purchase.item.enable") && e.doDelete) {
                        //item was removed
                        ctx.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.item.success")).replaceAll("%cp-args%", e.args[0]));
                    }
                }
                e.PAYWALL_OUTPUT = removedItem;
            } catch (Exception buyc) {
                ctx.debug.send(buyc, e.p, ctx);
                ctx.tex.sendString(e.p, ctx.tag + ctx.configHandler.config.getString("config.format.error") + " " + "commands: " + e.name);
                e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
            }
        }
    }

    public boolean removeItem(Player p, ItemStack itemToRemove, boolean ignoreNBT, boolean doDelete) {
        InventoryOperationResult result;

        if (ctx.inventorySaver.hasNormalInventory(p)) {
            result = removeItemFromInventory(p.getInventory().getContents(), itemToRemove, ignoreNBT, doDelete);
            p.getInventory().setContents(result.getInventory());
        } else {
            ItemStack[] savedInventory = ctx.inventorySaver.getNormalInventory(p);
            result = removeItemFromInventory(savedInventory, itemToRemove, ignoreNBT, doDelete);
            ctx.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), ctx.itemSerializer.itemStackArrayToBase64(result.getInventory()));
        }

        return result.isSuccess();  // Return the success status of the inventory operation
    }

    private InventoryOperationResult removeItemFromInventory(ItemStack[] inventory, ItemStack itemToRemove, boolean ignoreNBT, boolean doDelete) {
        int amountToRemove = itemToRemove.getAmount();
        int count = 0;

        for (ItemStack item : inventory) {
            if (item != null && ctx.itemCreate.isIdentical(item, itemToRemove, !ignoreNBT)) {
                count += item.getAmount();
            }
        }

        //return false if the player doesn't have enough items
        if (count < amountToRemove) {
            return new InventoryOperationResult(false, inventory);  // Not enough items, return with original inventory unchanged
        }

        //return true because there are enough items, but don't run the code to remove them
        if(!doDelete){
            return new InventoryOperationResult(true, inventory);
        }

        for (int i = 0; i < inventory.length; i++) {
            ItemStack currentItem = inventory[i];
            if (currentItem != null && ctx.itemCreate.isIdentical(currentItem, itemToRemove, !ignoreNBT)) {
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

        return new InventoryOperationResult(true, inventory);  // Return true and the modified inventory
    }
}
