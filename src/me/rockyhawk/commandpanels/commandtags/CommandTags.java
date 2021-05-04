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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CommandTags {
    CommandPanels plugin;
    public CommandTags(CommandPanels pl) {
        this.plugin = pl;
    }

    public void runCommand(Panel panel,Player p,String commandRAW){
        CommandTagEvent tags = new CommandTagEvent(plugin,panel,p,commandRAW);
        Bukkit.getPluginManager().callEvent(tags);
        if(!tags.commandTagUsed){
            Bukkit.dispatchCommand(p, plugin.tex.papi(panel,p,commandRAW.trim()));
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
    public int commandPayWall(Player p, String command) { //return 0 means no funds, 1 is they passed and 2 means paywall is not this command
        String tag = plugin.config.getString("config.format.tag") + " ";
        switch(command.split("\\s")[0]){
            case "paywall=": {
                //if player uses paywall= [price]
                try {
                    if (plugin.econ != null) {
                        if (plugin.econ.getBalance(p) >= Double.parseDouble(command.split("\\s")[1])) {
                            plugin.econ.withdrawPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                            plugin.tex.sendString(p,Objects.requireNonNull(plugin.config.getString("purchase.currency.success")).replaceAll("%cp-args%", command.split("\\s")[1]));
                            return 1;
                        } else {
                            plugin.tex.sendString(p,plugin.config.getString("purchase.currency.failure"));
                            return 0;
                        }
                    } else {
                        plugin.tex.sendString(p, tag + ChatColor.RED + "Paying Requires Vault and an Economy to work!");
                        return 0;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
                    return 0;
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
                            return 1;
                        } else {
                            plugin.tex.sendString(p,plugin.config.getString("purchase.tokens.failure"));
                            return 0;
                        }
                    } else {
                        plugin.tex.sendString(p, tag + ChatColor.RED + "Needs TokenManager to work!");
                        return 0;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
                    return 0;
                }
            }
            case "item-paywall=": {
                //if player uses item-paywall= [Material] [Amount] [Id]
                //or player can use item-paywall= [custom-item]
                try {
                    short id = 0;
                    if(command.split("\\s").length == 4){
                        id = Short.parseShort(command.split("\\s")[3]);
                    }

                    //create the item to be removed
                    ItemStack sellItem;
                    if(command.split("\\s").length == 2) {
                        sellItem = plugin.itemCreate.makeCustomItemFromConfig(null,plugin.openPanels.getOpenPanel(p.getName()).getConfig().getConfigurationSection("custom-item." + command.split("\\s")[1]), p, true, true, false);
                    }else{
                        sellItem = new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[1])), Integer.parseInt(command.split("\\s")[2]), id);
                    }
                    //this is not a boolean because it needs to return an int
                    int removedItem = 0;

                    //loop through items in the inventory
                    for(ItemStack content : p.getInventory().getContents()){

                        if(content == null){
                            //skip slot if empty
                            continue;
                        }

                        if(command.split("\\s").length == 2){
                            //if item paywall is custom item
                            if(plugin.itemCreate.isIdentical(sellItem,content)){
                                content.setAmount(content.getAmount() - sellItem.getAmount());
                                p.updateInventory();
                                removedItem = 1;
                                break;
                            }

                            //if custom item is an mmo item (1.14+ for the API)
                            try {
                                if (plugin.getServer().getPluginManager().isPluginEnabled("MMOItems") && plugin.openPanels.getOpenPanel(p.getName()).getConfig().getString("custom-item." + command.split("\\s")[1] + ".material").startsWith("mmo=")) {
                                    String customItemMaterial = plugin.openPanels.getOpenPanel(p.getName()).getConfig().getString("custom-item." + command.split("\\s")[1] + ".material");
                                    String mmoType = customItemMaterial.split("\\s")[1];
                                    String mmoID = customItemMaterial.split("\\s")[2];

                                    if (plugin.isMMOItem(content,mmoType,mmoID) && sellItem.getAmount() <= content.getAmount()) {
                                        content.setAmount(content.getAmount() - sellItem.getAmount());
                                        p.updateInventory();
                                        removedItem = 1;
                                        break;
                                    }
                                }
                            }catch (Exception ex){
                                plugin.debug(ex,p);
                            }

                        }else {
                            //if the item is a standard material
                            if (content.getType() == sellItem.getType()) {
                                if (sellItem.getAmount() <= content.getAmount()) {
                                    content.setAmount(content.getAmount() - sellItem.getAmount());
                                    p.updateInventory();
                                    removedItem = 1;
                                    break;
                                }
                            }
                        }
                    }

                    //send message and return
                    if(removedItem == 0){
                        plugin.tex.sendString(p, tag + plugin.config.getString("purchase.item.failure"));
                    }else{
                        plugin.tex.sendString(p,Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%",sellItem.getType().toString()));
                    }
                    return removedItem;
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
                    return 0;
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
                        return 1;
                    } else {
                        plugin.tex.sendString(p, plugin.config.getString("purchase.xp.failure"));
                        return 0;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
                    return 0;
                }
            }
        }
        return 2;
    }
}
