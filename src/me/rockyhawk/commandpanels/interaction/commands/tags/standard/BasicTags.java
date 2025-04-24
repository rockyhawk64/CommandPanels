package me.rockyhawk.commandpanels.interaction.commands.tags.standard;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.events.PanelInteractionEvent;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagEvent;
import me.rockyhawk.commandpanels.manager.PanelOpenType;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BasicTags implements Listener {
    Context ctx;
    public BasicTags(Context pl) {
        this.ctx = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("cpc") || e.name.equalsIgnoreCase("commandpanelclose")){
            e.commandTagUsed();

            //return if no panel open
            if(!ctx.openPanels.hasPanelOpen(e.p.getName(),PanelPosition.Top)){
                return;
            }

            //unclosable panels are at the Top only
            if(ctx.openPanels.getOpenPanel(e.p.getName(),PanelPosition.Top).getConfig().contains("panelType")){
                if(ctx.openPanels.getOpenPanel(e.p.getName(),PanelPosition.Top).getConfig().getStringList("panelType").contains("unclosable")){
                    ctx.openPanels.closePanelForLoader(e.p.getName(),PanelPosition.Top);
                    ctx.openPanels.skipPanelClose.add(e.p.getName());
                }
            }

            //this will close the current inventory
            e.p.closeInventory();
            ctx.openPanels.skipPanelClose.remove(e.p.getName());
            return;
        }
        if(e.name.equalsIgnoreCase("refresh")) {
            e.commandTagUsed();
            if(ctx.openPanels.hasPanelOpen(e.p.getName(),e.pos)) {
                ctx.createGUI.openGui(e.panel, e.p, e.pos, PanelOpenType.Refresh, 0);
            }
            if(ctx.inventorySaver.hasNormalInventory(e.p)){
                ctx.hotbar.updateHotbarItems(e.p);
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
            ctx.text.sendString(e.panel,e.pos,e.p,String.join(" ",e.args));
            return;
        }
        if(e.name.equalsIgnoreCase("broadcast=")) {
            e.commandTagUsed();
            Bukkit.broadcastMessage(ctx.text.placeholders(e.panel, e.pos, e.p,String.join(" ",e.args).trim()));
            return;
        }
        if(e.name.equalsIgnoreCase("broadcast-perm=")) {
            e.commandTagUsed();
            StringBuilder message = new StringBuilder();
            for(int i = 1; i < e.args.length; i++){
                message.append(e.args[i]).append(" ");
            }
            // <perm> <message>
            Bukkit.broadcast(ctx.text.placeholders(e.panel, e.pos, e.p,String.join(" ",message).trim()),String.valueOf(e.args[0]));
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
                ctx.debug.send(exc,e.p, ctx);
                e.p.sendMessage(ctx.tag + ctx.text.colour( ctx.configHandler.config.getString("config.format.error") + " op=: Error in op command!"));
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
                ctx.debug.send(s, e.p, ctx);
                ctx.text.sendMessage(e.p, ctx.configHandler.config.getString("config.format.error") + " " + "commands: " + e.args[0]);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("stopsound=")) {
            e.commandTagUsed();
            try {
                e.p.stopSound(Sound.valueOf(e.args[0]));
            } catch (Exception ss) {
                ctx.debug.send(ss, e.p, ctx);
                ctx.text.sendMessage(e.p, ctx.configHandler.config.getString("config.format.error") + " " + "commands: " + e.args[0]);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("event=")) {
            e.commandTagUsed();
            PanelInteractionEvent commandEvent = new PanelInteractionEvent(e.p, String.join(" ",e.args), e.panel);
            Bukkit.getPluginManager().callEvent(commandEvent);
            return;
        }
        if(e.name.equalsIgnoreCase("minimessage=")){
            e.commandTagUsed();
            //do mini message if conditions are met
            if(ctx.version.isAtLeast("1.18")){
                Audience player = (Audience) e.p; // Needed because the basic Player from the Event can't send Paper's Components
                Component parsedText = ctx.miniMessage.doMiniMessage(String.join(" ", e.args));
                player.sendMessage(parsedText);
            } else {
                ctx.text.sendString(e.p, ctx.tag + ChatColor.RED + "MiniMessage-Feature needs Paper 1.18 or newer to work!");
            }
        }
    }
}