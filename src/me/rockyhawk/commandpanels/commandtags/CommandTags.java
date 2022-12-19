package me.rockyhawk.commandpanels.commandtags;

import de.NeonnBukkit.CoinsAPI.API.CoinsAPI;
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
import org.bukkit.event.inventory.ClickType;
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

    //with the click type included
    public void runCommands(Panel panel, PanelPosition position,Player p, List<String> commands, ClickType click){
        for (String command : commands) {
            command = plugin.commandTags.hasCorrectClick(command,click);
            if(command.equals("")){
                //click type is wrong
                continue;
            }

            PaywallOutput val = plugin.commandTags.commandPayWall(panel,p,command);
            if(val == PaywallOutput.Blocked){
                break;
            }
            if(val == PaywallOutput.NotApplicable){
                plugin.commandTags.runCommand(panel,position,p, command);
            }
        }
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

    public String hasCorrectClick(String command, ClickType click){
        try {
            switch(command.split("\\s")[0]){
                case "right=":{
                    //if commands is for right clicking, remove the 'right=' and continue
                    command = command.replace("right= ", "");
                    if (click != ClickType.RIGHT) {
                        return "";
                    }
                    break;
                }
                case "rightshift=":{
                    //if commands is for right clicking, remove the 'right=' and continue
                    command = command.replace("rightshift= ", "");
                    if (click != ClickType.SHIFT_RIGHT) {
                        return "";
                    }
                    break;
                }
                case "left=":{
                    //if commands is for right clicking, remove the 'right=' and continue
                    command = command.replace("left= ", "");
                    if (click != ClickType.LEFT) {
                        return "";
                    }
                    break;
                }
                case "leftshift=":{
                    //if commands is for right clicking, remove the 'right=' and continue
                    command = command.replace("leftshift= ", "");
                    if (click != ClickType.SHIFT_LEFT) {
                        return "";
                    }
                    break;
                }
                case "middle=":{
                    command = command.replace("middle= ", "");
                    if (click != ClickType.MIDDLE) {
                        return "";
                    }
                    break;
                }
            }
            return command;
        } catch (Exception ex) {
            return "";
            //skip if you can't do this
        }
    }

    @SuppressWarnings("deprecation")
    public PaywallOutput commandPayWall(Panel panel, Player p, String rawCommand) { //return 0 means no funds, 1 is they passed and 2 means paywall is not this command

        //create new instance of command but with placeholders parsed
        String command = plugin.tex.placeholders(panel,PanelPosition.Top,p,rawCommand);
        switch(command.split("\\s")[0]){
            case "paywall=": {
                //if player uses paywall= [price]
                try {
                    if (plugin.econ != null) {
                        if (plugin.econ.getBalance(p) >= Double.parseDouble(command.split("\\s")[1])) {
                            plugin.econ.withdrawPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                            if(plugin.config.getBoolean("purchase.currency.enable")){
                                plugin.tex.sendString(panel, PanelPosition.Top, p, Objects.requireNonNull(plugin.config.getString("purchase.currency.success")).replaceAll("%cp-args%", command.split("\\s")[1]));
                            }
                            return PaywallOutput.Passed;
                        } else {
                            if(plugin.config.getBoolean("purchase.currency.enable")){
                                plugin.tex.sendString(panel, PanelPosition.Top, p, Objects.requireNonNull(plugin.config.getString("purchase.currency.failure")).replaceAll("%cp-args%", command.split("\\s")[1]));
                            }
                            return PaywallOutput.Blocked;
                        }
                    } else {
                        plugin.tex.sendString(p, plugin.tag + ChatColor.RED + "Paying Requires Vault and an Economy to work!");
                        return PaywallOutput.Blocked;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, plugin.tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
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
                            if(plugin.config.getBoolean("purchase.tokens.enable")){
                                plugin.tex.sendString(panel, PanelPosition.Top, p, Objects.requireNonNull(plugin.config.getString("purchase.tokens.success")).replaceAll("%cp-args%", command.split("\\s")[1]));
                            }
                            
                            return PaywallOutput.Passed;
                        } else {
                            if(plugin.config.getBoolean("purchase.tokens.enable")){
                                plugin.tex.sendString(panel, PanelPosition.Top, p, Objects.requireNonNull(plugin.config.getString("purchase.tokens.failure")).replaceAll("%cp-args%", command.split("\\s")[1]));
                            }
                            return PaywallOutput.Blocked;
                        }
                    } else {
                        plugin.tex.sendString(p, plugin.tag + ChatColor.RED + "Needs TokenManager to work!");
                        return PaywallOutput.Blocked;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, plugin.tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
                    return PaywallOutput.Blocked;
                }
            }
            case "coinpaywall=": {
                //if player uses coinpaywall= [price]
                try {
                    if (plugin.getServer().getPluginManager().isPluginEnabled("CoinsAPINB")) {
                        int balance = CoinsAPI.getCoins(p.getUniqueId().toString());
                        if (balance >= Double.parseDouble(command.split("\\s")[1])) {
                            CoinsAPI.removeCoins(p.getUniqueId().toString(), (int) Long.parseLong(command.split("\\s")[1]));
                            //if the message is empty don't send
                            if(plugin.config.getBoolean("purchase.coins.enable")){
                                plugin.tex.sendString(panel, PanelPosition.Top, p, Objects.requireNonNull(plugin.config.getString("purchase.coins.success")).replaceAll("%cp-args%", command.split("\\s")[1]));
                            }
                            return PaywallOutput.Passed;
                        } else {
                            if(plugin.config.getBoolean("purchase.coins.enable")){
                                plugin.tex.sendString(panel, PanelPosition.Top, p, Objects.requireNonNull(plugin.config.getString("purchase.coins.failure")).replaceAll("%cp-args%", command.split("\\s")[1]));
                            }
                            return PaywallOutput.Blocked;
                        }
                    } else {
                        plugin.tex.sendString(p, plugin.tag + ChatColor.RED + "Needs CoinAPI to work!");
                        return PaywallOutput.Blocked;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, plugin.tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
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
                    if(Material.matchMaterial(command.split("\\s")[1]) == null) {
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

                        if(Material.matchMaterial(command.split("\\s")[1]) == null){
                            //item-paywall is a custom item as it is not a material
                            if(plugin.itemCreate.isIdentical(sellItem,cont.get(f))){
                                int sellItemAmount = sellItem.getAmount();
                                if(command.split("\\s").length == 3){
                                    sellItemAmount = Integer.parseInt(command.split("\\s")[2]);
                                }
                                if (sellItemAmount <= cont.get(f).getAmount()) {
                                    if (plugin.inventorySaver.hasNormalInventory(p)) {
                                        p.getInventory().getItem(f).setAmount(cont.get(f).getAmount() - sellItemAmount);
                                        p.updateInventory();
                                    } else {
                                        cont.get(f).setAmount(cont.get(f).getAmount() - sellItemAmount);
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
                        if(plugin.config.getBoolean("purchase.item.enable")){
                            plugin.tex.sendString(panel, PanelPosition.Top, p, Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%", command.split("\\s")[1]));
                        }
                    }else{
                        if(plugin.config.getBoolean("purchase.item.enable")){
                            plugin.tex.sendString(panel, PanelPosition.Top, p, Objects.requireNonNull(plugin.config.getString("purchase.item.failure")).replaceAll("%cp-args%", command.split("\\s")[1]));
                        }
                    }
                    return removedItem;
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, plugin.tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
                    return PaywallOutput.Blocked;
                }
            }
            case "xp-paywall=": {
                //if player uses xp-paywall= <price> <levels:points>
                try {
                    int balance;
                    if(command.split("\\s")[2].startsWith("level")){
                        balance = p.getLevel();
                    }else{
                        balance = getPlayerExp(p);
                    }
                    if (balance >= Integer.parseInt(command.split("\\s")[1])) {
                        if(command.split("\\s")[2].startsWith("level")){
                            p.setLevel(p.getLevel() - Integer.parseInt(command.split("\\s")[1]));
                        }else{
                            removePlayerExp(p,Integer.parseInt(command.split("\\s")[1]));
                        }
                        //if the message is empty don't send
                        if(plugin.config.getBoolean("purchase.xp.enable")){
                            plugin.tex.sendString(panel, PanelPosition.Top, p, Objects.requireNonNull(plugin.config.getString("purchase.xp.success")).replaceAll("%cp-args%", command.split("\\s")[1]));
                        }
                        return PaywallOutput.Passed;
                    } else {
                        if(plugin.config.getBoolean("purchase.xp.enable")){
                            plugin.tex.sendString(panel, PanelPosition.Top, p, Objects.requireNonNull(plugin.config.getString("purchase.xp.failure")).replaceAll("%cp-args%", command.split("\\s")[1]));
                        }
                        return PaywallOutput.Blocked;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, plugin.tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
                    return PaywallOutput.Blocked;
                }
            }
            case "data-paywall=": {
                //if player uses data-paywall= <data> <amount>
                try {
                    if (Double.parseDouble(plugin.panelData.getUserData(p.getUniqueId(),command.split("\\s")[1])) >= Double.parseDouble(command.split("\\s")[2])) {
                        plugin.panelData.doDataMath(p.getUniqueId(),command.split("\\s")[1],"-" + plugin.tex.placeholdersNoColour(panel,PanelPosition.Top,p,command.split("\\s")[2]));
                        //if the message is empty don't send
                        if(plugin.config.getBoolean("purchase.data.enable")){
                            plugin.tex.sendString(panel, PanelPosition.Top, p, Objects.requireNonNull(plugin.config.getString("purchase.data.success")).replaceAll("%cp-args%", command.split("\\s")[1]));
                        }
                        return PaywallOutput.Passed;
                    } else {
                        if(plugin.config.getBoolean("purchase.data.enable")){
                            plugin.tex.sendString(panel, PanelPosition.Top, p, Objects.requireNonNull(plugin.config.getString("purchase.data.failure")).replaceAll("%cp-args%", command.split("\\s")[1]));
                        }
                        return PaywallOutput.Blocked;
                    }
                } catch (Exception buyc) {
                    plugin.debug(buyc,p);
                    plugin.tex.sendString(p, plugin.tag + plugin.config.getString("config.format.error") + " " + "commands: " + command);
                    return PaywallOutput.Blocked;
                }
            }
        }
        return PaywallOutput.NotApplicable;
    }

    //Experience math is a bit doggy doo doo so these will help to calculate values
    // Calculate total experience up to a level

    // @author thelonelywolf@https://github.com/TheLonelyWolf1
    // @date 06 August 2021
    private int getExpAtLevel(int level){
        if(level <= 16){
            return (int) (Math.pow(level,2) + 6*level);
        } else if(level <= 31){
            return (int) (2.5*Math.pow(level,2) - 40.5*level + 360.0);
        } else {
            return (int) (4.5*Math.pow(level,2) - 162.5*level + 2220.0);
        }
    }

    // Calculate amount of EXP needed to level up
    private int getExpToLevelUp(int level){
        if(level <= 15){
            return 2*level+7;
        } else if(level <= 30){
            return 5*level-38;
        } else {
            return 9*level-158;
        }
    }

    // Calculate player's current EXP amount
    private int getPlayerExp(Player player){
        int exp = 0;
        int level = player.getLevel();

        // Get the amount of XP in past levels
        exp += getExpAtLevel(level);

        // Get amount of XP towards next level
        exp += Math.round(getExpToLevelUp(level) * player.getExp());

        return exp;
    }

    // Take EXP
    private int removePlayerExp(Player player, int exp){
        // Get player's current exp
        int currentExp = getPlayerExp(player);

        // Reset player's current exp to 0
        player.setExp(0);
        player.setLevel(0);

        // Give the player their exp back, with the difference
        int newExp = currentExp - exp;
        player.giveExp(newExp);

        // Return the player's new exp amount
        return newExp;
    }
}
