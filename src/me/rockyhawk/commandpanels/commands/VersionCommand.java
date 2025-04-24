package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VersionCommand implements CommandExecutor {
    Context ctx;
    public VersionCommand(Context pl) { this.ctx = pl; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (label.equalsIgnoreCase("cpv") || label.equalsIgnoreCase("commandpanelversion") || label.equalsIgnoreCase("cpanelv")) {
            if(args.length == 0) {
                if (sender.hasPermission("commandpanel.version")) {
                    //version command
                    String latestVersion = ctx.updater.versionChecker.getLatestVersion(false);
                    sender.sendMessage(ctx.text.colour(ctx.tag));
                    sender.sendMessage(ChatColor.GREEN + "This Version   " + ChatColor.GRAY + ctx.plugin.getDescription().getVersion());
                    sender.sendMessage(ChatColor.GREEN + "Latest Version " + ChatColor.GRAY + latestVersion);
                    sender.sendMessage(ChatColor.GRAY + "-------------------");
                    sender.sendMessage(ChatColor.GREEN + "Developer " + ChatColor.GRAY + "RockyHawk");
                    sender.sendMessage(ChatColor.GREEN + "Command   " + ChatColor.GRAY + "/cp");
                } else {
                    sender.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
                }
            }else if(args.length == 1){
                if (sender.hasPermission("commandpanel.update")) {
                    if (args[0].equals("cancel")) {
                        ctx.updater.downloadVersionManually = null;
                        sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.GREEN + "Will not download a new version on restart."));
                    } else {
                        ctx.updater.downloadVersionManually = args[0];
                        sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.GREEN + "Downloading version " + ChatColor.GRAY + args[0] + ChatColor.GREEN + " upon server restart."));
                    }
                }else{
                    sender.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
                }
            }else{
                sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.RED + "Usage: /cpv [update:latest:cancel]"));
            }
            return true;
        }
        return true;
    }
}
