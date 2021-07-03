package me.rockyhawk.commandpanels.commandtags.tags.standard;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.PanelCommandEvent;
import me.rockyhawk.commandpanels.commandtags.CommandTagEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BasicTags implements Listener {
    CommandPanels plugin;
    public BasicTags(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("cpc")){
            e.commandTagUsed();
            //this will close the current inventory
            e.p.closeInventory();
            return;
        }
        if(e.name.equalsIgnoreCase("refresh")) {
            e.commandTagUsed();
            plugin.createGUI.openGui(plugin.openPanels.getOpenPanel(e.p.getName()), e.p, 0, 0);
            return;
        }
        if(e.name.equalsIgnoreCase("console=")) {
            e.commandTagUsed();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.join(" ",e.args));
            return;
        }
        if(e.name.equalsIgnoreCase("send=")) {
            e.commandTagUsed();
            e.p.chat(String.join(" ",e.args));
            return;
        }
        if(e.name.equalsIgnoreCase("sudo=")) {
            e.commandTagUsed();
            e.p.chat("/" + String.join(" ",e.args));
            return;
        }
        if(e.name.equalsIgnoreCase("msg=")) {
            e.commandTagUsed();
            e.p.sendMessage(String.join(" ",e.args));
            return;
        }
        if(e.name.equalsIgnoreCase("op=")) {
            e.commandTagUsed();
            //if player uses op= it will perform command as op
            boolean isop = e.p.isOp();
            try {
                e.p.setOp(true);
                Bukkit.dispatchCommand(e.p,String.join(" ",e.args));
                e.p.setOp(isop);
            } catch (Exception exc) {
                e.p.setOp(isop);
                plugin.debug(exc,e.p);
                e.p.sendMessage(plugin.tag + plugin.tex.colour( plugin.config.getString("config.format.error") + " op=: Error in op command!"));
            }
            return;
        }
        if(e.name.equalsIgnoreCase("sound=")) {
            e.commandTagUsed();
            try {
                e.p.playSound(e.p.getLocation(), Sound.valueOf(e.args[0]), 1F, 1F);
            } catch (Exception s) {
                plugin.debug(s, e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.args[0]);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("stopsound=")) {
            e.commandTagUsed();
            try {
                e.p.stopSound(Sound.valueOf(e.args[0]));
            } catch (Exception ss) {
                plugin.debug(ss, e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.args[0]);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("event=")) {
            e.commandTagUsed();
            PanelCommandEvent commandEvent = new PanelCommandEvent(e.p, e.args[0], e.panel);
            Bukkit.getPluginManager().callEvent(commandEvent);
        }
    }
}
