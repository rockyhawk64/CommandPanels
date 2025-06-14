package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PanelCommand implements CommandExecutor {
    Context ctx;

    public PanelCommand(Context pl) {
        this.ctx = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //Check for blanket permission
        if(!sender.hasPermission("commandpanel.panel")){
            sender.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
            return true;
        }
        //below is going to go through the files and find the right one
        Panel panel = null;
        if (args.length != 0) { //check to make sure the person hasn't just left it empty
            for (Panel tempPanel : ctx.plugin.panelList) {
                if (tempPanel.getName().equals(args[0])) {
                    panel = tempPanel;
                    break;
                }
            }
        } else {
            helpMessage(sender);
            return true;
        }
        if (panel == null) {
            sender.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.nopanel")));
            return true;
        }
        boolean disableCommand = false;
        if (panel.getConfig().contains("panelType")) {
            if (panel.getConfig().getStringList("panelType").contains("nocommand")) {
                //do not allow command with noCommand, console is an exception
                disableCommand = true;
            }
        }
        //below will start the command, once it got the right file and panel
        if (!(sender instanceof Player)) {
            //do console command
            if (args.length == 2) {
                if (!args[1].equals("item")) {
                    if (args[1].equalsIgnoreCase("all")) {
                        for (Player player : Bukkit.getOnlinePlayers())
                            ctx.openPanel.open(sender, player, panel.copy(), PanelPosition.Top);
                    } else
                        ctx.openPanel.open(sender, Bukkit.getServer().getPlayer(args[1]), panel.copy(), PanelPosition.Top);
                } else {
                    sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.RED + "Usage: /cp <panel> [item] [player|all]"));
                }
                return true;
            } else if (args.length == 3) {
                if (args[1].equals("item")) {
                    if (args[2].equalsIgnoreCase("all")) {
                        // if the argument is all open the panel for all of the players
                        for (Player player : Bukkit.getOnlinePlayers())
                            ctx.openPanel.open(sender, player, panel.copy(), PanelPosition.Top);
                    } else
                        ctx.hotbar.give.giveHotbarItem(sender, Bukkit.getServer().getPlayer(args[2]), panel.copy());
                } else {
                    sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.RED + "Usage: /cp <panel> item [player|all]"));
                }
                return true;
            } else {
                sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.RED + "Please execute command directed to a Player!"));
                return true;
            }
        } else {
            //get player
            Player p = (Player) sender;
            //do player command
            if (args.length == 1) {
                if (!disableCommand) {
                    ctx.openPanel.open(sender, p, panel.copy(), PanelPosition.Top);
                }
                return true;
            } else if (args.length == 2) {
                if (args[1].equals("item")) {

                    ctx.hotbar.give.giveHotbarItem(sender, p, panel.copy());
                } else {
                    if (!disableCommand) {
                        if (args[1].equalsIgnoreCase("all")) {
                            // if the argument is all open the panel for all of the players
                            for (Player player : Bukkit.getOnlinePlayers())
                                ctx.openPanel.open(sender, player, panel.copy(), PanelPosition.Top);
                        } else
                            ctx.openPanel.open(sender, Bukkit.getServer().getPlayer(args[1]), panel.copy(), PanelPosition.Top);
                    }
                }
                return true;
            } else if (args.length == 3) {
                if (args[2].equalsIgnoreCase("all")) {
                    // if the argument is all open the panel for all of the players
                    for (Player player : Bukkit.getOnlinePlayers())
                        ctx.hotbar.give.giveHotbarItem(sender, player, panel.copy());
                } else
                    ctx.hotbar.give.giveHotbarItem(sender, Bukkit.getServer().getPlayer(args[2]), panel.copy());
                return true;
            }
        }
        sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.RED + "Usage: /cp <panel> [player|all:item] [player|all]"));
        return true;
    }

    private void helpMessage(CommandSender p) {
        p.sendMessage(ctx.text.colour( ctx.tag + ChatColor.GREEN + "Commands:"));
        p.sendMessage(ChatColor.GOLD + "/cp <panel> [player:item] [player] " + ChatColor.WHITE + "Open a command panel.");
        if (p.hasPermission("commandpanel.reload")) {
            p.sendMessage(ChatColor.GOLD + "/cpr " + ChatColor.WHITE + "Reloads plugin config.");
        }
        if (p.hasPermission("commandpanel.generate")) {
            p.sendMessage(ChatColor.GOLD + "/cpg <rows> " + ChatColor.WHITE + "Generate GUI from popup menu.");
        }
        if (p.hasPermission("commandpanel.version")) {
            p.sendMessage(ChatColor.GOLD + "/cpv " + ChatColor.WHITE + "Display the current version.");
        }
        if (p.hasPermission("commandpanel.refresh")) {
            p.sendMessage(ChatColor.GOLD + "/cpu <player> [position:all] " + ChatColor.WHITE + "Update a panel for a player while it is still open.");
        }
        if (p.hasPermission("commandpanel.update")) {
            p.sendMessage(ChatColor.GOLD + "/cpv latest " + ChatColor.WHITE + "Download the latest update upon server reload/restart.");
            p.sendMessage(ChatColor.GOLD + "/cpv [version:cancel] " + ChatColor.WHITE + "Download an update upon server reload/restart.");
        }
        if (p.hasPermission("commandpanel.edit")) {
            p.sendMessage(ChatColor.GOLD + "/cpe <panel file> " + ChatColor.WHITE + "Export panel to the Online Editor.");
        }
        if (p.hasPermission("commandpanel.import")) {
            p.sendMessage(ChatColor.GOLD + "/cpi <file name> <URL> " + ChatColor.WHITE + "Downloads a panel from a raw link online.");
        }
        if (p.hasPermission("commandpanel.list")) {
            p.sendMessage(ChatColor.GOLD + "/cpl " + ChatColor.WHITE + "Lists the currently loaded panels.");
        }
        if (p.hasPermission("commandpanel.data")) {
            p.sendMessage(ChatColor.GOLD + "/cpdata " + ChatColor.WHITE + "Change panel data for a user.");
        }
        if (p.hasPermission("commandpanel.debug")) {
            p.sendMessage(ChatColor.GOLD + "/cpd " + ChatColor.WHITE + "Enable and Disable debug mode globally.");
        }
        if (p.hasPermission("commandpanel.block.add")) {
            p.sendMessage(ChatColor.GOLD + "/cpb add <panel> " + ChatColor.WHITE + "Add panel to a block being looked at.");
        }
        if (p.hasPermission("commandpanel.block.remove")) {
            p.sendMessage(ChatColor.GOLD + "/cpb remove " + ChatColor.WHITE + "Removes any panel assigned to a block looked at.");
        }
        if (p.hasPermission("commandpanel.block.list")) {
            p.sendMessage(ChatColor.GOLD + "/cpb list " + ChatColor.WHITE + "List blocks that will open panels.");
        }
    }
}
