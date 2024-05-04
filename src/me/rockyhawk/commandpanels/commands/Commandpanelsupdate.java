package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelOpenType;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;


public class Commandpanelsupdate implements CommandExecutor {
    CommandPanels plugin;
    public Commandpanelsupdate(CommandPanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("cpu") || label.equalsIgnoreCase("commandpanelupdate") || label.equalsIgnoreCase("cpanelu")) {
            if (sender.hasPermission("commandpanel.refresh")) {
                String name;
                Player targetPlayer;
                try {
                    name = args[0];
                    targetPlayer = Bukkit.getPlayer(name);
                }catch (Exception e){
                    sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Player was not found."));
                    return true;
                }
                assert targetPlayer != null;

                PanelPosition pp;
                if(args[1].equalsIgnoreCase("all")){
                    for(PanelPosition papo : PanelPosition.values()){
                        if(plugin.openPanels.hasPanelOpen(name, papo)) {
                            plugin.createGUI.openGui(plugin.openPanels.getOpenPanel(name, papo), targetPlayer, papo, PanelOpenType.Refresh, 0);
                        }
                    }
                } else {
                    try {
                        pp = PanelPosition.valueOf(args[1]);
                    }catch (Exception e){
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Panel position not found."));
                        return true;
                    }

                    if(plugin.openPanels.hasPanelOpen(name, pp)) {
                        plugin.createGUI.openGui(plugin.openPanels.getOpenPanel(name, pp), targetPlayer, pp, PanelOpenType.Refresh, 0);
                    }
                }


                if(plugin.inventorySaver.hasNormalInventory(targetPlayer)){
                    plugin.hotbar.updateHotbarItems(targetPlayer);
                }

                //Successfully refreshed panel for targetPlayer.getName()
                sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Successfully refreshed panel for " + targetPlayer.getName() + "."));
            }else{
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            }
            return true;
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Usage: /cpu <Playername> <Position/ALL>"));
        return true;
    }
}