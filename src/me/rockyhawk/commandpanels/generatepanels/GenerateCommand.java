package me.rockyhawk.commandpanels.generatepanels;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;


public class GenerateCommand implements CommandExecutor {
    Context ctx;
    public GenerateCommand(Context pl) { this.ctx = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Please execute command as a Player!"));
            return true;
        }
        Player p = (Player) sender;

        if (p.hasPermission("commandpanel.generate")) {
            if (args.length == 1) {
                //command /cpg
                try {
                    if (Integer.parseInt(args[0]) >= 1 && Integer.parseInt(args[0]) <= 6) {
                        Inventory i = Bukkit.createInventory(null, Integer.parseInt(args[0]) * 9, "Generate New Panel");
                        p.openInventory(i);
                    } else {
                        p.sendMessage(ctx.tex.colour( ctx.tag + ChatColor.RED + "Please use integer from 1-6."));
                    }
                }catch(Exception exc){
                    p.sendMessage(ctx.tex.colour( ctx.tag + ChatColor.RED + "Please use integer from 1-6."));
                }
                return true;
            }else if (args.length == 0) {
                if (this.ctx.generator.generateMode.contains(p)) {
                    this.ctx.generator.generateMode.remove(p);
                    p.sendMessage(ctx.tex.colour( ctx.tag + ChatColor.GREEN + "Generate Mode Disabled!"));
                } else {
                    this.ctx.generator.generateMode.add(p);
                    p.sendMessage(ctx.tex.colour( ctx.tag + ChatColor.GREEN + "Generate Mode Enabled!"));
                }
                return true;
            }
            p.sendMessage(ctx.tex.colour( ctx.tag + ChatColor.RED + "Usage: /cpg [rows]"));
        }else{
            p.sendMessage(ctx.tex.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
        }
        return true;
    }
}