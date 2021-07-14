package me.rockyhawk.commandpanels.commandtags;

import me.realized.tokenmanager.api.TokenManager;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.commandtags.tags.economy.BuyCommandTags;
import me.rockyhawk.commandpanels.commandtags.tags.economy.BuyItemTags;
import me.rockyhawk.commandpanels.commandtags.tags.economy.SellItemTags;
import me.rockyhawk.commandpanels.commandtags.tags.other.DataTags;
import me.rockyhawk.commandpanels.commandtags.tags.other.PlaceholderTags;
import me.rockyhawk.commandpanels.commandtags.tags.other.SpecialTags;
import me.rockyhawk.commandpanels.commandtags.tags.standard.BasicTags;
import me.rockyhawk.commandpanels.commandtags.tags.standard.BungeeTags;
import me.rockyhawk.commandpanels.commandtags.tags.standard.ItemTags;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CommandTags {
    CommandPanels plugin;
    public CommandTags(CommandPanels pl) {
        this.plugin = pl;
    }

    public void runCommands(Panel panel, PanelPosition position,Player p, List<String> commands){
        for (String command : commands) {
            PaywallOutput val = plugin.commandTags.commandPayWall(panel,p,command);
            if(val == PaywallOutput.Blocked){
                break;
            }
            if(val == PaywallOutput.NotApplicable){
                plugin.commandTags.runCommand(panel,position,p, command);
            }
        }
    }

    public void runCommand(Panel panel, PanelPosition position,Player p, String commandRAW){
        CommandTagEvent tags = new CommandTagEvent(plugin,panel,position,p,commandRAW);
        Bukkit.getPluginManager().callEvent(tags);
        if(!tags.commandTagUsed){
            Bukkit.dispatchCommand(p, plugin.tex.placeholders(panel,position,p,commandRAW.trim()));
        }
    }

    //do this on startup to load listeners
    public void registerBuiltInTags(){
        plugin.getServer().getPluginManager().registerEvents(new BuyCommandTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BuyItemTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SellItemTags(plugin), plugin);

        plugin.getServer().getPluginManager().registerEvents(new DataTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlaceholderTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SpecialTags(plugin), plugin);

        plugin.getServer().getPluginManager().registerEvents(new BasicTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BungeeTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ItemTags(plugin), plugin);
    }

    @SuppressWarnings("deprecation")
    public PaywallOutput commandPayWall(Panel panel, Player p, String command) { //return 0 means no funds, 1 is they passed and 2 means paywall is not this command
        String tag = plugin.config.getString("config.format.tag") + " ";
        switch(command.split("\\s")[0]){
            case "paywall=": {
                //if player uses paywall= [price]
                try {
                    if (plugin.econ != null) {
                        if (plugin.econ.getBalance(p) >= Double.parseDouble(command.split("\\s")[1])) {
                            plugin.econ.withdrawPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                            plugin.tex.sendString(p,Objects.requireNonNull(plugin.config.getString("purchase.currency.success")).replaceAll("%cp-args%", command.split("\\s")[1]));
                            return PaywallOutput.Passed;
                        } else {
                            plugin.tex.sendString(p,plugin.config.getString("purchase.currency.failure"));
                            return PaywallOutput.Blocked;
                        }
                    } else {
                        plugin.tex.sendString(p, tag + ChatColor.RED + "Paying Requires Vault and an Economy to work!");
                        return PaywallOutput.Blocked;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
                    return PaywallOutput.Blocked;
                }
            }
            case "tokenpaywall=": {
                //if player uses tokenpaywall= [price]
                try {
                    if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                        TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                        assert api != null;
                        int balance = Integer.parseInt(Long.toString(api.getTokens(p).orElse(0)));
                        if (balance >= Double.parseDouble(command.split("\\s")[1])) {
                            api.removeTokens(p, Long.parseLong(command.split("\\s")[1]));
                            //if the message is empty don't send
                            plugin.tex.sendString(p,Objects.requireNonNull(plugin.config.getString("purchase.tokens.success")).replaceAll("%cp-args%", command.split("\\s")[1]));
                            return PaywallOutput.Passed;
                        } else {
                            plugin.tex.sendString(p,plugin.config.getString("purchase.tokens.failure"));
                            return PaywallOutput.Blocked;
                        }
                    } else {
                        plugin.tex.sendString(p, tag + ChatColor.RED + "Needs TokenManager to work!");
                        return PaywallOutput.Blocked;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
                    return PaywallOutput.Blocked;
                }
            }
            case "item-paywall=": {
                //if player uses item-paywall= [Material] [Amount] [Id]
                //player can use item-paywall= [custom-item]
                List<ItemStack> cont = new ArrayList<>(Arrays.asList(plugin.inventorySaver.getNormalInventory(p)));
                try {
                    short id = 0;
                    if(command.split("\\s").length == 4){
                        id = Short.parseShort(command.split("\\s")[3]);
                    }

                    //create the item to be removed
                    ItemStack sellItem;
                    if(command.split("\\s").length == 2) {
                        sellItem = plugin.itemCreate.makeCustomItemFromConfig(panel,PanelPosition.Top,panel.getConfig().getConfigurationSection("custom-item." + command.split("\\s")[1]), p, true, true, false);
                    }else{
                        sellItem = new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[1])), Integer.parseInt(command.split("\\s")[2]), id);
                    }
                    //this is not a boolean because it needs to return an int
                    PaywallOutput removedItem = PaywallOutput.Blocked;

                    //loop through items in the inventory
                    for(int f = 0; f < 36; f++){

                        if(cont.get(f) == null){
                            //skip slot if empty
                            continue;
                        }

                        if(command.split("\\s").length == 2){
                            //if item paywall is custom item
                            if(plugin.itemCreate.isIdentical(sellItem,cont.get(f))){
                                if (sellItem.getAmount() <= cont.get(f).getAmount()) {
                                    if (plugin.inventorySaver.hasNormalInventory(p)) {
                                        p.getInventory().getItem(f).setAmount(cont.get(f).getAmount() - sellItem.getAmount());
                                        p.updateInventory();
                                    } else {
                                        cont.get(f).setAmount(cont.get(f).getAmount() - sellItem.getAmount());
                                        plugin.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                                    }
                                    removedItem = PaywallOutput.Passed;
                                    break;
                                }
                            }

                            //if custom item is an mmo item (1.14+ for the API)
                            try {
                                if (plugin.getServer().getPluginManager().isPluginEnabled("MMOItems") && panel.getConfig().getString("custom-item." + command.split("\\s")[1] + ".material").startsWith("mmo=")) {
                                    String customItemMaterial = panel.getConfig().getString("custom-item." + command.split("\\s")[1] + ".material");
                                    String mmoType = customItemMaterial.split("\\s")[1];
                                    String mmoID = customItemMaterial.split("\\s")[2];

                                    if (plugin.isMMOItem(cont.get(f),mmoType,mmoID) && sellItem.getAmount() <= cont.get(f).getAmount()) {
                                        if(plugin.inventorySaver.hasNormalInventory(p)){
                                            p.getInventory().getItem(f).setAmount(cont.get(f).getAmount() - sellItem.getAmount());
                                            p.updateInventory();
                                        }else{
                                            cont.get(f).setAmount(cont.get(f).getAmount() - sellItem.getAmount());
                                            plugin.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                                        }
                                        removedItem = PaywallOutput.Passed;
                                        break;
                                    }
                                }
                            }catch (Exception ex){
                                plugin.debug(ex,p);
                            }

                        }else {
                            //if the item is a standard material
                            if (cont.get(f).getType() == sellItem.getType()) {
                                if (sellItem.getAmount() <= cont.get(f).getAmount()) {
                                    if(plugin.inventorySaver.hasNormalInventory(p)){
                                        p.getInventory().getItem(f).setAmount(cont.get(f).getAmount() - sellItem.getAmount());
                                        p.updateInventory();
                                    }else{
                                        cont.get(f).setAmount(cont.get(f).getAmount() - sellItem.getAmount());
                                        plugin.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                                    }
                                    removedItem = PaywallOutput.Passed;
                                    break;
                                }
                            }
                        }
                    }

                    //send message and return
                    if(removedItem == PaywallOutput.Blocked){
                        plugin.tex.sendString(p, tag + plugin.config.getString("purchase.item.failure"));
                    }else{
                        plugin.tex.sendString(p,Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%",sellItem.getType().toString()));
                    }
                    return removedItem;
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
                    return PaywallOutput.Blocked;
                }
            }
            case "xp-paywall=": {
                //if player uses xp-paywall= [price]
                try {
                    int balance = p.getLevel();
                    if (balance >= Integer.parseInt(command.split("\\s")[1])) {
                        p.setLevel(p.getLevel() - Integer.parseInt(command.split("\\s")[1]));
                        //if the message is empty don't send
                        plugin.tex.sendString(p,Objects.requireNonNull(plugin.config.getString("purchase.xp.success")).replaceAll("%cp-args%", command.split("\\s")[1]));
                        return PaywallOutput.Passed;
                    } else {
                        plugin.tex.sendString(p, plugin.config.getString("purchase.xp.failure"));
                        return PaywallOutput.Blocked;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
                    return PaywallOutput.Blocked;
                }
            }
        }
        return PaywallOutput.NotApplicable;
    }
}
