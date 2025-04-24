package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugCommand implements CommandExecutor {
    Context ctx;
    public DebugCommand(Context pl) { this.ctx = pl; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("commandpanel.debug")) {
            if (args.length == 0) {
                //command /cpd
                if(!(sender instanceof Player)) {
                    ctx.debug.consoleDebug = !ctx.debug.consoleDebug;
                    sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.GREEN + "Global Debug Mode: " + ctx.debug.consoleDebug));
                    return true;
                }

                Player p = (Player)sender;
                if(ctx.debug.isEnabled(p)){
                    ctx.debug.debugSet.remove(p);
                    sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.GREEN + "Personal Debug Mode Disabled!"));
                }else{
                    ctx.debug.debugSet.add(p);
                    sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.GREEN + "Personal Debug Mode Enabled!"));
                }
            }else{
                sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Usage: /cpd"));
            }
        }else{
            sender.sendMessage(ctx.tex.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
        }
        return true;
    }
}