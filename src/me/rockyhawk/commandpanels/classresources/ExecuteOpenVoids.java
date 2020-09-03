package me.rockyhawk.commandpanels.classresources;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class ExecuteOpenVoids {
    CommandPanels plugin;
    public ExecuteOpenVoids(CommandPanels pl) {
        this.plugin = pl;
    }

    //this is the main method to open a panel
    public void openCommandPanel(CommandSender sender, Player p, String panels, YamlConfiguration cf, boolean sendOpenedMessage){
        String tag = plugin.config.getString("config.format.tag") + " ";
        if (sender.hasPermission("commandpanel.panel." + cf.getString("panels." + panels + ".perm"))) {
            //if the sender has OTHER perms, or if sendOpenedMessage is false, implying it is not for another person
            if(sender.hasPermission("commandpanel.other") || !sendOpenedMessage) {
                try {
                    if (cf.contains("panels." + panels + ".disabled-worlds")) {
                        List<String> disabledWorlds = cf.getStringList("panels." + panels + ".disabled-worlds");
                        if (disabledWorlds.contains(p.getWorld().getName())) {
                            //panel cannot be used in the players world!
                            if (Objects.requireNonNull(plugin.config.getString("config.disabled-world-message")).equalsIgnoreCase("true")) {
                                sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Panel is disabled in the world!"));
                            }
                            return;
                        }
                    }
                }catch(NullPointerException offlinePlayer){
                    //SKIP because player is offline
                    sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.notitem")));
                    return;
                }
                try {
                    if (cf.contains("panels." + panels + ".sound-on-open")) {
                        //play sound when panel is opened
                        if(!Objects.requireNonNull(cf.getString("panels." + panels + ".sound-on-open")).equalsIgnoreCase("off")) {
                            try {
                                p.playSound(p.getLocation(), Sound.valueOf(Objects.requireNonNull(cf.getString("panels." + panels + ".sound-on-open")).toUpperCase()), 1F, 1F);
                            } catch (Exception s) {
                                p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " " + "sound-on-open: " + cf.getString("panels." + panels + ".sound-on-open")));
                            }
                        }
                    }
                    if (cf.contains("panels." + panels + ".commands-on-open")) {
                        //execute commands on panel open
                        try {
                            List<String> commands = cf.getStringList("panels." + panels + ".commands-on-open");
                            for (int i = 0; commands.size() - 1 >= i; i++) {
                                int val = plugin.commandTags.commandPayWall(p,commands.get(i));
                                if(val == 0){
                                    break;
                                }
                                if(val == 2){
                                    plugin.commandTags.commandTags(p, commands.get(i));
                                }
                            }
                        }catch(Exception s){
                            p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " " + "commands-on-open: " + cf.getString("panels." + panels + ".commands-on-open")));
                        }
                    }
                    plugin.openGui(panels, p, cf,1,0);
                    if(sendOpenedMessage) {
                        sender.sendMessage(plugin.papi( tag + ChatColor.GREEN + "Panel Opened for " + p.getDisplayName()));
                    }
                } catch (Exception r) {
                    plugin.debug(r);
                    sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.notitem")));
                }
            }else{
                sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.perms")));
            }
            return;
        }
        sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.perms")));
    }

    //this will give a hotbar item to a player
    public void giveHotbarItem(CommandSender sender, Player p, String panels, YamlConfiguration cf, boolean sendGiveMessage){
        String tag = plugin.config.getString("config.format.tag") + " ";
        if (sender.hasPermission("commandpanel.item." + cf.getString("panels." + panels + ".perm")) && cf.contains("panels." + panels + ".open-with-item")) {
            try {
                if (cf.contains("panels." + panels + ".disabled-worlds")) {
                    List<String> disabledWorlds = cf.getStringList("panels." + panels + ".disabled-worlds");
                    if (disabledWorlds.contains(p.getWorld().getName())) {
                        //panel cannot be used in the players world!
                        if (Objects.requireNonNull(plugin.config.getString("config.disabled-world-message")).equalsIgnoreCase("true")) {
                            sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Panel is disabled in the world!"));
                        }
                        return;
                    }
                }
            }catch(NullPointerException offlinePlayer){
                //SKIP because player is offline
                sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.notitem")));
                return;
            }
            ItemStack s;
            try {
                s = plugin.itemCreate.makeItemFromConfig(Objects.requireNonNull(cf.getConfigurationSection("panels." + panels + ".open-with-item")), p, false, true);
            }catch(Exception n){
                sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " open-with-item: material"));
                return;
            }
            plugin.setName(s, cf.getString("panels." + panels + ".open-with-item.name"), cf.getStringList("panels." + panels + ".open-with-item.lore"),p,false, true);
            //if the sender has OTHER perms, or if sendGiveMessage is false, implying it is not for another person
            if(sender.hasPermission("commandpanel.other") || !sendGiveMessage) {
                try {
                    if(cf.contains("panels." + panels + ".open-with-item.stationary")) {
                        p.getInventory().setItem(Integer.parseInt(Objects.requireNonNull(cf.getString("panels." + panels + ".open-with-item.stationary"))), s);
                    }else{
                        p.getInventory().addItem(s);
                    }
                    if(sendGiveMessage) {
                        sender.sendMessage(plugin.papi( tag + ChatColor.GREEN + "Item Given to " + p.getDisplayName()));
                    }
                } catch (Exception r) {
                    sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.notitem")));
                }
            }else{
                sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.perms")));
            }
            return;
        }
        if (!cf.contains("panels." + panels + ".open-with-item")) {
            sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.noitem")));
            return;
        }
        sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.perms")));
    }
}
