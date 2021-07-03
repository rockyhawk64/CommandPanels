package me.rockyhawk.commandpanels.generatepanels;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;


public class Commandpanelsgenerate implements CommandExecutor {
    CommandPanels plugin;
    public Commandpanelsgenerate(CommandPanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Please execute command as a Player!"));
            return true;
        }
        Player p = (Player) sender;
        if (label.equalsIgnoreCase("cpg") || label.equalsIgnoreCase("commandpanelgenerate") || label.equalsIgnoreCase("cpanelg")) {
            if (p.hasPermission("commandpanel.generate")) {
                if (args.length == 1) {
                    //command /cpg
                    try {
                        if (Integer.parseInt(args[0]) >= 1 && Integer.parseInt(args[0]) <= 6) {
                            Inventory i = Bukkit.createInventory(null, Integer.parseInt(args[0]) * 9, "Generate New Panel");
                            p.openInventory(i);
                        } else {
                            p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Please use integer from 1-6."));
                        }
                    }catch(Exception exc){
                        p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Please use integer from 1-6."));
                    }
                    return true;
                }else if (args.length == 0) {
                    if (this.plugin.generateMode.contains(p)) {
                        this.plugin.generateMode.remove(p);
                        p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Generate Mode Disabled!"));
                    } else {
                        this.plugin.generateMode.add(p);
                        p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Generate Mode Enabled!"));
                    }
                    return true;
                }
                p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Usage: /cpg [rows]"));
                return true;
            }else{
                p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                return true;
            }
        }
        p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Usage: /cpg [rows]"));
        return true;
    }
}