package me.rockyhawk.commandpanels.commandtags.tags.standard;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.PanelCommandEvent;
import me.rockyhawk.commandpanels.classresources.SerializerUtils;
import me.rockyhawk.commandpanels.commandtags.CommandTagEvent;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelOpenType;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

            //unclosable panels are at the Top only
            if(plugin.openPanels.getOpenPanel(e.p.getName(),PanelPosition.Top).getConfig().contains("panelType")){
                if(plugin.openPanels.getOpenPanel(e.p.getName(),PanelPosition.Top).getConfig().getStringList("panelType").contains("unclosable")){
                    plugin.openPanels.closePanelForLoader(e.p.getName(),PanelPosition.Top);
                    plugin.openPanels.skipPanelClose.add(e.p.getName());
                }
            }

            //this will close the current inventory
            e.p.closeInventory();
            plugin.openPanels.skipPanelClose.remove(e.p.getName());
            return;
        }
        if(e.name.equalsIgnoreCase("refresh")) {
            e.commandTagUsed();
            if(plugin.openPanels.hasPanelOpen(e.p.getName(),e.pos)) {
                plugin.createGUI.openGui(e.panel, e.p, e.pos, PanelOpenType.Refresh, 0);
            }
            if(plugin.inventorySaver.hasNormalInventory(e.p)){
                plugin.hotbar.updateHotbarItems(e.p);
            }
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
            plugin.tex.sendString(e.panel,e.pos,e.p,String.join(" ",e.args));
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
                if(e.args.length == 3){
                    //volume (0.0 to 1.0), pitch (0.5 to 2.0)
                    e.p.playSound(e.p.getLocation(), Sound.valueOf(e.args[0]), Float.parseFloat(e.args[1]), Float.parseFloat(e.args[2]));
                }else{
                    e.p.playSound(e.p.getLocation(), Sound.valueOf(e.args[0]), 1F, 1F);
                }
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
            PanelCommandEvent commandEvent = new PanelCommandEvent(e.p, String.join(" ",e.args), e.panel);
            Bukkit.getPluginManager().callEvent(commandEvent);
            return;
        }
        if(e.name.equalsIgnoreCase("minimessage=")){
            e.commandTagUsed();
            if(plugin.legacy.LOCAL_VERSION.greaterThanOrEqualTo(MinecraftVersions.v1_18) && Bukkit.getServer().getVersion().contains("Paper")){
                Audience player = (Audience) e.p; // Needed because the basic Player from the Event can't send Paper's Components
                Component parsedText = SerializerUtils.serializeText(String.join(" ",e.args));
                player.sendMessage(parsedText);
            }else{
                plugin.tex.sendString(e.p, plugin.tag + ChatColor.RED + "MiniMessage-Feature needs Paper 1.18 or newer to work!");
            }
        }
    }
}