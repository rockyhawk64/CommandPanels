package me.rockyhawk.commandpanels.commandtags.tags.economy;

import me.realized.tokenmanager.api.TokenManager;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.CommandTagEvent;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SellItemTags implements Listener {
    CommandPanels plugin;
    public SellItemTags(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("sell=")){
            e.commandTagUsed();
            //if player uses sell= it will be eg. sell= <total cashback> <item> <amount of item> [enchanted:KNOCKBACK:1] [potion:JUMP] [custom-data:#]
            try {
                if (plugin.econ != null) {
                    int sold = removeItem(e.p, e.args, false);
                    if (sold <= 0) {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.failure")));
                    } else {
                        plugin.econ.depositPlayer(e.p, Double.parseDouble(e.args[0]));
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%", e.args[1]).replaceAll("%cp-args2%", "$" + e.args[0]));
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
            //if player uses sell-all= it will be eg. sell-all= <Per Item Cashback> <item> [enchanted:KNOCKBACK:1] [potion:JUMP]
            try {
                if (plugin.econ != null) {
                    int sold = removeItem(e.p, e.args, true);
                    if (sold <= 0) {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.failure")));
                    } else {
                        plugin.econ.depositPlayer(e.p, Double.parseDouble(e.args[0]) * sold);
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%", e.args[1]).replaceAll("%cp-args2%", "$" + Double.parseDouble(e.args[0]) * sold));
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
            //if player uses tokensell= it will be eg. tokensell= <cashback> <item> <amount of item> [enchanted:KNOCKBACK:1] [potion:JUMP]
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    final TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    int sold = removeItem(e.p, e.args, false);
                    if (sold <= 0) {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.failure")));
                    } else {
                        assert api != null;
                        api.addTokens(e.p, Long.parseLong(e.args[0]));
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%", e.args[1]));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Selling Requires TokenManager to work!");
                }
            } catch (Exception sell) {
                plugin.debug(sell,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
            }
        }
    }

    //returns false if player does not have item
    private int removeItem(Player p, String[] args, boolean removeAll){
        //get inventory slots and then an empty list to store slots that have the item to sell
        List<ItemStack> cont = new ArrayList<>(Arrays.asList(plugin.inventorySaver.getNormalInventory(p)));
        List<ItemStack> remCont = new ArrayList<>();
        byte id = -1;
        String potion = "false";
        int customData = 0;
        boolean noCustom = false;
        for(String argsTemp : args){
            if(argsTemp.startsWith("potion:")){
                potion = argsTemp.replace("potion:","");
            }
            if (argsTemp.startsWith("id:")) {
                id = Byte.parseByte(argsTemp.replace("id:", ""));
            }
            if (argsTemp.startsWith("custom-data:")) {
                customData = Integer.parseInt(argsTemp.replace("custom-data:", ""));
            }
            if (argsTemp.contains("NOCUSTOMDATA")) {
                noCustom = true;
            }
        }

        //create an itemstack of the item to sell and the amount to sell (0 if all as args[2] will not be an amount)
        ItemStack sellItem = new ItemStack(Objects.requireNonNull(Material.matchMaterial(args[1])), removeAll ? 0 : Integer.parseInt(args[2]));
        int remainingAmount = removeAll ? 0 : sellItem.getAmount();
        for (int f = 0; f < 36; f++) {
            ItemStack itm = cont.get(f);
            ItemStack remItm;
            if (itm != null && itm.getType().equals(sellItem.getType())) {
                remItm = new ItemStack(itm.getType(), itm.getAmount(), (short)f);
                //check to ensure any extensions are checked
                try {
                    if (!potion.equals("false")) {
                        PotionMeta potionMeta = (PotionMeta) itm.getItemMeta();
                        assert potionMeta != null;
                        if (!potionMeta.getBasePotionData().getType().name().equalsIgnoreCase(potion)) {
                            p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Your item has the wrong potion effect"));
                            return 0;
                        }
                    }
                    //Check if the item matches the id set. If not continue to next in loop.
                    if(id != -1 && itm.getDurability() != id){
                        continue;
                    }
                    //Check if noCustom is set and if the item has custom data. If so continue to next in loop.
                    if(noCustom && cont.get(f).hasItemMeta()){
                        if(Objects.requireNonNull(cont.get(f).getItemMeta()).hasCustomModelData()){
                            continue;
                        }
                    }
                    //Check if custom model data is set and if the item has that data. If not continue to next in loop.
                    if (customData != 0) {
                        if (!itm.hasItemMeta()) {
                            continue;
                        } else {
                            if(Objects.requireNonNull(itm.getItemMeta()).getCustomModelData() != customData){
                                continue;
                            }
                        }
                    }
                }catch(Exception exc){
                    //skip if it cannot do unless plugin.debug is enabled
                    plugin.debug(exc,p);
                }

                remCont.add(remItm);

                //if the remaining amount has been reached, break otherwise sell all
                if (!removeAll) {
                    remainingAmount -= remItm.getAmount();
                    if (remainingAmount <= 0) {
                        break;
                    }
                } else {
                    sellItem.setAmount(sellItem.getAmount() + remItm.getAmount());
                }
            }
        }

        if(remainingAmount <= 0){
            int removedItems = 0;
            for (int f = 0; f <= remCont.size() - 1; f++) {
                ItemStack remItm = remCont.get(f);
                if(f == remCont.size() - 1){
                    if(plugin.inventorySaver.hasNormalInventory(p)){
                        p.getInventory().getItem(remItm.getDurability()).setAmount(remItm.getAmount() - sellItem.getAmount());
                        p.updateInventory();
                    }else{
                        cont.get(remItm.getDurability()).setAmount(remItm.getAmount() - sellItem.getAmount());
                        plugin.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                    }
                } else {
                    if(plugin.inventorySaver.hasNormalInventory(p)){
                        p.getInventory().getItem(remItm.getDurability()).setAmount(0);
                        p.updateInventory();
                    }else{
                        cont.get(remItm.getDurability()).setAmount(0);
                        plugin.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                    }
                }
                removedItems += remItm.getAmount();
                sellItem.setAmount(sellItem.getAmount() - remItm.getAmount());
            }
            return removedItems;
        }
        return 0;
    }
}
