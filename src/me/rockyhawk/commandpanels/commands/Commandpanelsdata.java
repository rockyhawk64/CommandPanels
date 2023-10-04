package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class Commandpanelsdata implements CommandExecutor {
    CommandPanels plugin;

    public Commandpanelsdata(CommandPanels pl) {
        this.plugin = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("commandpanel.data")) {
            boolean sendPlayerMessage = true;
            //check to make sure args isn't empty first
            //if the first argument is -s it will not send a message to the sender
            if(args.length != 0) {
                if (args[0].equalsIgnoreCase("-s")) {
                    args = Arrays.copyOfRange(args, 1, args.length);
                    sendPlayerMessage = false;
                }
            }
            int count = 0;
            if (args.length == 2) {
                //for the clear command
                if (args[0].equals("clear")) {
                    if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("online")) {
                        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                            if (args[1].equalsIgnoreCase("online") && !player.isOnline()) continue;
                            plugin.panelData.clearData(plugin.panelData.getOffline(player.getName()));
                            count++;
                        }
                    } else
                        plugin.panelData.clearData(plugin.panelData.getOffline(args[1]));
                    if (sendPlayerMessage) {
                        sender.sendMessage(plugin.tex.colour(plugin.tag
                                + ChatColor.GREEN + "Cleared all data for "
                                + ChatColor.WHITE + (count == 0 ? args[1] : count + "" + ChatColor.GREEN + " players")));
                    }
                    return true;
                }
            } else if (args.length == 3) {
                //for the remove command
                if (args[0].equals("remove")) {
                    if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("online")) {
                        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                            if (args[1].equalsIgnoreCase("online") && !player.isOnline()) continue;
                            plugin.panelData.delUserData(plugin.panelData.getOffline(player.getName()), args[2]);
                            count++;
                        }
                    } else
                        plugin.panelData.delUserData(plugin.panelData.getOffline(args[1]), args[2]);
                    if (sendPlayerMessage) {
                        sender.sendMessage(plugin.tex.colour(plugin.tag
                                + ChatColor.GREEN + "Removed "
                                + ChatColor.WHITE + args[2]
                                + ChatColor.GREEN + " from "
                                + ChatColor.WHITE + (count == 0 ? args[1] : count + "" + ChatColor.GREEN + " players")));
                    }
                    return true;
                } else if (args[0].equals("get")) {
                    //for the get command
                    sender.sendMessage(plugin.tex.colour(plugin.tag
                            + ChatColor.GREEN + "Value of data is "
                            + ChatColor.WHITE + plugin.panelData.getUserData(plugin.panelData.getOffline(args[1]), args[2])));
                    return true;
                }
            } else if (args.length == 4) {
                if (args[0].equals("set")) {
                    //for set command
                    if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("online")) {
                        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                            if (args[1].equalsIgnoreCase("online") && !player.isOnline()) continue;
                            plugin.panelData.setUserData(plugin.panelData.getOffline(player.getName()), args[2], args[3], true);
                            count++;
                        }
                    } else {
                        plugin.panelData.setUserData(plugin.panelData.getOffline(args[1]), args[2], args[3], true);
                    }
                    if (sendPlayerMessage) {
                        sender.sendMessage(plugin.tex.colour(plugin.tag
                                + ChatColor.GREEN + "Set "
                                + ChatColor.WHITE + args[2]
                                + ChatColor.GREEN + " to "
                                + ChatColor.WHITE + args[3]
                                + ChatColor.GREEN + " for "
                                + ChatColor.WHITE + (count == 0 ? args[1] : count + "" + ChatColor.GREEN + " players")));
                    }
                } else {
                    //for add command
                    if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("online")) {

                        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                            if (args[1].equalsIgnoreCase("online") && !player.isOnline()) continue;
                            plugin.panelData.setUserData(plugin.panelData.getOffline(player.getName()), args[2], args[3], false);
                            count++;
                        }
                    } else
                        plugin.panelData.setUserData(plugin.panelData.getOffline(args[1]), args[2], args[3], false);
                    if (sendPlayerMessage) {
                        sender.sendMessage(plugin.tex.colour(plugin.tag
                                + ChatColor.GREEN + "Set "
                                + ChatColor.WHITE + args[2]
                                + ChatColor.GREEN + " to "
                                + ChatColor.WHITE + args[3])
                                + ChatColor.GREEN + " for "
                                + ChatColor.WHITE + (count == 0 ? args[1] : count + "" + ChatColor.GREEN + " players")
                                + " if it did not exist already");
                    }
                }
                return true;
            }
            sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Usage: /cpdata <set:add:get:remove:clear> <player|all|online> <data> [value]"));
        } else {
            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
        }
        return true;
    }
}