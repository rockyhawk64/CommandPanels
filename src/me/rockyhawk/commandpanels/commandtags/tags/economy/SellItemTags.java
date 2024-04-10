package me.rockyhawk.commandpanels.commandtags.tags.economy;

import me.realized.tokenmanager.api.TokenManager;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.CommandTagEvent;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SellItemTags implements Listener {
    CommandPanels plugin;
    public SellItemTags(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("sell=")){
            e.commandTagUsed();
            //if player uses sell= it will be eg. sell= <total cashback> <item> <amount of item> [IGNORENBT]
            try {
                if (plugin.econ != null) {
                    int sold = removeItem(e.p, e, false);
                    if (sold <= 0) {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.failure")));
                    } else {
                        plugin.econ.depositPlayer(e.p, Double.parseDouble(e.args[0]));
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%", sold + " " + e.args[1]).replaceAll("%cp-args2%", "$" + e.args[0]));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Selling Requires Vault and an Economy to work!");
                }
            } catch (Exception sell) {
                plugin.debug(sell,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("sellall=")){
            e.commandTagUsed();
            //if player uses sell-all= it will be eg. sell-all= <Per Item Cashback> <item> [IGNORENBT]
            try {
                if (plugin.econ != null) {
                    int sold = removeItem(e.p, e, true);
                    if (sold <= 0) {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.failure")));
                    } else {
                        plugin.econ.depositPlayer(e.p, Double.parseDouble(e.args[0]) * sold);
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%", sold + " " + e.args[1]).replaceAll("%cp-args2%", String.valueOf(Double.parseDouble(e.args[0]) * sold)));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Selling Requires Vault and an Economy to work!");
                }
            } catch (Exception sell) {
                plugin.debug(sell,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("tokensell=")) {
            e.commandTagUsed();
            //if player uses tokensell= it will be eg. tokensell= <cashback> <item> <amount of item> [IGNORENBT]
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    final TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    int sold = removeItem(e.p, e, false);
                    if (sold <= 0) {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.failure")));
                    } else {
                        assert api != null;
                        api.addTokens(e.p, Long.parseLong(e.args[0]));
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%", sold + " " + e.args[1]));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Selling Requires TokenManager to work!");
                }
            } catch (Exception sell) {
                plugin.debug(sell,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
            }
        }
        if(e.name.equalsIgnoreCase("tokensellall=")){
            e.commandTagUsed();
            //if player uses tokensellall= it will be eg. tokensellall= <cashback> <item> [IGNORENBT]
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    final TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    int sold = removeItem(e.p, e, true);
                    if (sold <= 0) {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.failure")));
                    } else {
                        assert api != null;
                        api.addTokens(e.p, Long.parseLong(e.args[0]));
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%", sold + " " + e.args[1]));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Selling Requires TokenManager to work!");
                }
            } catch (Exception sell) {
                plugin.debug(sell,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
            }
            return;
        }
    }

    //returns false if player does not have item
    private int removeItem(Player p, CommandTagEvent e, boolean removeAll){
        String[] args = e.args;
        //get inventory slots and then an empty list to store slots that have the item to sell
        List<ItemStack> cont = new ArrayList<>(Arrays.asList(plugin.inventorySaver.getNormalInventory(p)));
        HashMap<Integer, ItemStack> remCont = new HashMap<>();
        int count = 0;

        try {
            int id = -1;
            for (String val : args) {
                if (val.startsWith("id:")) {
                    id = Integer.parseInt(val.substring(3));
                }
            }

            ItemStack sellItem;
            if (Material.matchMaterial(args[1]) == null) {
                sellItem = plugin.itemCreate.makeCustomItemFromConfig(e.panel, PanelPosition.Top, e.panel.getConfig().getConfigurationSection("custom-item." + args[1]), e.p, true, true, false);
                if(!removeAll){
                    sellItem.setAmount(Integer.parseInt(args[2]));
                } else {
                    sellItem.setAmount(1);
                }

            } else {
                if(!removeAll){
                    sellItem = new ItemStack(Objects.requireNonNull(Material.matchMaterial(args[1])), Integer.parseInt(args[2]));
                } else {
                    sellItem = new ItemStack(Objects.requireNonNull(Material.matchMaterial(args[1])));
                }
            }

            //This is here for when people want to take nbt items like spawners with types in a check for spawners.
            boolean ignoreNBT = false;
            for(String arg : args){
                if (arg.equalsIgnoreCase("IGNORENBT")) {
                    ignoreNBT = true;
                    break;
                }
            }

            int remainingAmount = removeAll ? 0 : sellItem.getAmount();
            //loop through items in the inventory
            for (int f = 0; f < 36; f++) {

                if (cont.get(f) == null) {
                    //skip slot if empty
                    continue;
                }

                ItemStack itm = cont.get(f);

                if (Material.matchMaterial(args[1]) == null) {
                    //if custom item is a mmo item (1.14+ for the API)
                    try {
                        if (plugin.getServer().getPluginManager().isPluginEnabled("MMOItems") && e.panel.getConfig().getString("custom-item." + args[1] + ".material").startsWith("mmo=")) {
                            String customItemMaterial = e.panel.getConfig().getString("custom-item." + args[1] + ".material");
                            String mmoType = customItemMaterial.split("\\s")[1];
                            String mmoID = customItemMaterial.split("\\s")[2];

                            if (plugin.isMMOItem(itm, mmoType, mmoID)) {
                                ItemStack add = new ItemStack(p.getInventory().getItem(f).getType(), p.getInventory().getItem(f).getAmount());
                                remainingAmount -= add.getAmount();
                                remCont.put(f,add);
                                if (remainingAmount <= 0 && !removeAll) {
                                    break;
                                }
                            }
                            continue; //This stops the other custom item section from reading and adding false numbers.
                        }
                    } catch (Exception ex) {
                        plugin.debug(ex, p);
                    }

                    //item-paywall is a custom item as it is not a material
                    if (plugin.itemCreate.isIdentical(sellItem, itm, Objects.requireNonNull(e.panel.getConfig().getConfigurationSection("custom-item." + args[1])).contains("nbt"))) {
                        ItemStack add = new ItemStack(p.getInventory().getItem(f).getType(), p.getInventory().getItem(f).getAmount());
                        remainingAmount -= add.getAmount();
                        remCont.put(f,add);
                        if (remainingAmount <= 0 && !removeAll) {
                            break;
                        }
                    }

                } else {
                    //if the item is a standard material
                    if (itm.getType() == sellItem.getType()) {
                        //If item has custom meta continue to next item.
                        if(itm.hasItemMeta() && !ignoreNBT){
                            continue;
                        }

                        //Check if the item matches the id set. If not continue to next in loop.
                        if (id != -1 && itm.getDurability() != id) {
                            continue;
                        }

                        //Adding item to the remove list then checking if we have reached the required amount.
                        ItemStack add = new ItemStack(itm.getType(), itm.getAmount());
                        remainingAmount -= add.getAmount();
                        remCont.put(f,add);
                        if (remainingAmount <= 0 && !removeAll) {
                            break;
                        }
                    }
                }
            }

            if (remainingAmount <= 0 && !removeAll) {
                //Only remove if passed requirement
                for(Map.Entry<Integer, ItemStack> entry : remCont.entrySet()) {
                    ItemStack remItem = entry.getValue();

                    //Check if its the last item in the loop and only subtract the remaining amount.
                    if (sellItem.getAmount() < remItem.getAmount()) {
                        if (plugin.inventorySaver.hasNormalInventory(p)) {
                            p.getInventory().getItem(entry.getKey()).setAmount(remItem.getAmount() - sellItem.getAmount());
                            count += remItem.getAmount();
                            p.updateInventory();
                        } else {
                            cont.get(entry.getKey()).setAmount(remItem.getAmount() - sellItem.getAmount());
                            count += remItem.getAmount();
                            plugin.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                        }
                    } else { //If its anywhere but the last in loop just get rid of the items.
                        if (plugin.inventorySaver.hasNormalInventory(p)) {
                            p.getInventory().setItem(entry.getKey(), null);
                            count += remItem.getAmount();
                            p.updateInventory();
                        } else {
                            cont.remove(entry.getValue());
                            count += remItem.getAmount();
                            plugin.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                        }
                    }

                    sellItem.setAmount(sellItem.getAmount() - remItem.getAmount());
                }
            } else if(remainingAmount <= 0){
                //Remove all. Used for sellall=
                for(Map.Entry<Integer, ItemStack> entry : remCont.entrySet()) {
                    ItemStack remItem = entry.getValue();

                    if (plugin.inventorySaver.hasNormalInventory(p)) {
                        p.getInventory().setItem(entry.getKey(), null);
                        count += remItem.getAmount();
                        p.updateInventory();
                    } else {
                        cont.remove(entry.getValue());
                        count += remItem.getAmount();
                        plugin.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                    }
                }
            }

            //Return how many were sold.
            return count;
        } catch (Exception buyc) {
            //Error somewhere in sell= process.
            plugin.debug(buyc, p);
            plugin.tex.sendString(p, plugin.tag + plugin.config.getString("config.format.error") + " " + "commands: sell=/sellall=");
            return 0; //Return 0 showing failure.
        }
    }
}
