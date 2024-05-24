package me.rockyhawk.commandpanels.commandtags.paywalls.itempaywall;

import me.rockyhawk.commandpanels.CommandPanels;
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
    CommandPanels plugin;
    public ItemPaywall(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(PaywallEvent e){
        if(e.name.equalsIgnoreCase("item-paywall=")){
            //if player uses item-paywall= [Material] [Amount] <id:#> <IGNORENBT> WILL NOT TAKE CUSTOM ITEMS. IGNORENBT lets nbt items through. Useful for spawner edge cases.
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
                    sellItem = plugin.itemCreate.makeCustomItemFromConfig(e.panel, PanelPosition.Top, e.panel.getConfig().getConfigurationSection("custom-item." + e.args[0]), e.p, true, true, false);
                    sellItem.setAmount(Integer.parseInt(e.args[1]));
                } else {
                    //If normal just set material.
                    sellItem = new ItemStack(Objects.requireNonNull(Material.matchMaterial(e.args[0])), Integer.parseInt(e.args[1]));
                }

                //try to remove the item and determine outcome
                PaywallOutput removedItem = PaywallOutput.Blocked;
                if (e.doDelete){
                    if(removeItem(e.p, sellItem, ignoreNBT)){
                        removedItem = PaywallOutput.Passed;
                    }
                }

                //send message and return
                if (removedItem == PaywallOutput.Blocked) {
                    if (plugin.config.getBoolean("purchase.item.enable")) {
                        //no item found to remove
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.failure")));
                    }
                } else {
                    if (plugin.config.getBoolean("purchase.item.enable") && e.doDelete) {
                        //item was removed
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%", e.args[0]));
                    }
                }
                e.PAYWALL_OUTPUT = removedItem;
            } catch (Exception buyc) {
                plugin.debug(buyc, e.p);
                plugin.tex.sendString(e.p, plugin.tag + plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
                e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
            }
        }
    }

    public boolean removeItem(Player p, ItemStack itemToRemove, boolean ignoreNBT) {
        InventoryOperationResult result;

        if (plugin.inventorySaver.hasNormalInventory(p)) {
            result = removeItemFromInventory(p.getInventory().getContents(), itemToRemove, ignoreNBT);
            p.getInventory().setContents(result.getInventory());
        } else {
            ItemStack[] savedInventory = plugin.inventorySaver.getNormalInventory(p);
            result = removeItemFromInventory(savedInventory, itemToRemove, ignoreNBT);
            plugin.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(result.getInventory()));
        }

        return result.isSuccess();  // Return the success status of the inventory operation
    }

    private InventoryOperationResult removeItemFromInventory(ItemStack[] inventory, ItemStack itemToRemove, boolean ignoreNBT) {
        int amountToRemove = itemToRemove.getAmount();
        int count = 0;

        for (ItemStack item : inventory) {
            if (item != null && plugin.itemCreate.isIdentical(item, itemToRemove, !ignoreNBT)) {
                count += item.getAmount();
            }
        }

        if (count < amountToRemove) {
            return new InventoryOperationResult(false, inventory);  // Not enough items, return with original inventory unchanged
        }

        for (int i = 0; i < inventory.length; i++) {
            ItemStack currentItem = inventory[i];
            if (currentItem != null && plugin.itemCreate.isIdentical(currentItem, itemToRemove, !ignoreNBT)) {
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
