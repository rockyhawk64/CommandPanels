package me.rockyhawk.commandpanels.classresources;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.api.PanelOpenedEvent;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelOpenType;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.Objects;

public class ExecuteOpenVoids {
    CommandPanels plugin;
    public ExecuteOpenVoids(CommandPanels pl) {
        this.plugin = pl;
    }

    //this is the main method to open a panel
    public void openCommandPanel(CommandSender sender, Player p, Panel panel, PanelPosition position, boolean openForOtherUser){
        if(p == null){
            sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Player not found."));
            return;
        }
        if(p.isSleeping()){
            //avoid plugin glitches when sleeping
            return;
        }
        if((plugin.debug.isEnabled(sender) || plugin.config.getBoolean("config.auto-update-panels")) && panel.getFile() != null){
            //reload the panel if debug is enabled
            panel.setConfig(YamlConfiguration.loadConfiguration(panel.getFile()));
        }
        if (!sender.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm"))) {
            if(panel.getConfig().getString("custom-messages.perms") != null) {
        		sender.sendMessage(plugin.tex.colour(plugin.tag + panel.getConfig().getString("custom-messages.perms")));
            }else {
        		sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            }
            return;
        }
        //if the sender has OTHER perms, or if sendOpenedMessage is false, implying it is not for another person
        if(sender.hasPermission("commandpanel.other") || !openForOtherUser) {
            //check for disabled worlds
            if(!plugin.panelPerms.isPanelWorldEnabled(p,panel.getConfig())){
                if(panel.getConfig().getString("custom-messages.perms") != null) {
        		    sender.sendMessage(plugin.tex.colour(plugin.tag + panel.getConfig().getString("custom-messages.perms")));
                }else {
        		    sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                }
                return;
            }

            if(position != PanelPosition.Top && !plugin.openPanels.hasPanelOpen(p.getName(),PanelPosition.Top)){
                sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Cannot open a panel without a panel at the top already."));
                return;
            }

            //close any foreign GUIs for CommandPanels
            if(!plugin.openPanels.hasPanelOpen(p.getName(),PanelPosition.Top) && p.getOpenInventory().getType() != InventoryType.CRAFTING){
                p.closeInventory();
            }

            //fire PanelOpenedEvent
            PanelOpenedEvent openedEvent = new PanelOpenedEvent(p,panel,position);
            Bukkit.getPluginManager().callEvent(openedEvent);
            if(openedEvent.isCancelled()){
                return;
            }

            //do these commands before the panel loads
            beforeLoadCommands(panel,position,p);

            try {
                //create and open the GUI
                plugin.createGUI.openGui(panel, p, position,PanelOpenType.Normal,0);

                //execute commands once the panel opens
                if (panel.getConfig().contains("commands-on-open")) {
                    try {
                        plugin.commandTags.runCommands(panel,position,p, panel.getConfig().getStringList("commands-on-open"));
                    }catch(Exception s){
                        p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " " + "commands-on-open: " + panel.getConfig().getString("commands-on-open")));
                    }
                }

                if (panel.getConfig().contains("sound-on-open")) {
                    //play sound when panel is opened
                    if(!Objects.requireNonNull(panel.getConfig().getString("sound-on-open")).equalsIgnoreCase("off")) {
                        try {
                            p.playSound(p.getLocation(), Sound.valueOf(Objects.requireNonNull(panel.getConfig().getString("sound-on-open")).toUpperCase()), 1F, 1F);
                        } catch (Exception s) {
                            p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " " + "sound-on-open: " + panel.getConfig().getString("sound-on-open")));
                        }
                    }
                }

                if(openForOtherUser) {
                    sender.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Panel Opened for " + p.getDisplayName()));
                }
            } catch (Exception r) {
                plugin.debug(r,null);
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error")));
                plugin.openPanels.closePanelForLoader(p.getName(),position);
                p.closeInventory();
            }
        }else{
            if(panel.getConfig().getString("custom-messages.perms") != null) {
        		sender.sendMessage(plugin.tex.colour(plugin.tag + panel.getConfig().getString("custom-messages.perms")));
            }else {
        		sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            }
        }
    }

    //this will give a hotbar item to a player
    public void giveHotbarItem(CommandSender sender, Player p, Panel panel, boolean sendGiveMessage){
        if (sender.hasPermission("commandpanel.item." + panel.getConfig().getString("perm")) && panel.getConfig().contains("open-with-item")) {
            //check for disabled worlds
            if(!plugin.panelPerms.isPanelWorldEnabled(p,panel.getConfig())){
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
                return;
            }

            //if the sender has OTHER perms, or if sendGiveMessage is false, implying it is not for another person
            if(sender.hasPermission("commandpanel.other") || !sendGiveMessage) {
                try {
                    if(panel.getConfig().contains("open-with-item.stationary")) {
                        p.getInventory().setItem(Integer.parseInt(Objects.requireNonNull(panel.getConfig().getString("open-with-item.stationary"))), panel.getHotbarItem(p));
                    }else{
                        p.getInventory().addItem(panel.getHotbarItem(p));
                    }
                    if(sendGiveMessage) {
                        sender.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Item Given to " + p.getDisplayName()));
                    }
                } catch (Exception r) {
                    sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.notitem")));
                }
            }else{
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            }
            return;
        }
        if (!panel.getConfig().contains("open-with-item")) {
            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.noitem")));
            return;
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
    }

    public void beforeLoadCommands(Panel panel,PanelPosition pos, Player p){
        if (panel.getConfig().contains("pre-load-commands")) {
            try {
                plugin.commandTags.runCommands(panel,pos,p, panel.getConfig().getStringList("pre-load-commands"));
            }catch(Exception s){
                plugin.debug(s,p);
            }
        }
    }
}
