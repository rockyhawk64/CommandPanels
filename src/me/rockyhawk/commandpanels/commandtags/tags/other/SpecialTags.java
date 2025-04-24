package me.rockyhawk.commandpanels.commandtags.tags.other;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.commandtags.CommandTagEvent;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class SpecialTags implements Listener {
    Context ctx;
    public SpecialTags(Context pl) {
        this.ctx = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("open=")) {
            e.commandTagUsed();
            //if player uses open= it will open the panel, with the option to add custom placeholders
            String panelName = e.args[0];
            String cmd = String.join(" ",e.args).replace(e.args[0] + " ","").trim();

            Panel openPanel = null;
            PanelPosition openPosition = e.pos;
            for(Panel pane : ctx.plugin.panelList){
                if(pane.getName().equals(panelName)){
                    openPanel = pane.copy();
                }
            }
            if(openPanel == null){
                return;
            }

            Character[] cm = convertToCharacterArray(cmd.toCharArray());
            for(int i = 0; i < cm.length; i++){
                if(cm[i].equals('[')){
                    String contents = cmd.substring(i+1, i+cmd.substring(i).indexOf(']'));
                    //do not change the placeholder
                    String placeholder = contents.substring(0,contents.indexOf(':'));
                    //only convert placeholders for the value
                    String value = ctx.tex.placeholders(e.panel,e.pos,e.p,contents.substring(contents.indexOf(':')+1));
                    openPanel.placeholders.addPlaceholder(placeholder,value);
                    i = i+contents.length()-1;
                }else if(cm[i].equals('{')){
                    String contents = cmd.substring(i+1, i+cmd.substring(i).indexOf('}'));
                    openPosition = PanelPosition.valueOf(contents);
                    i = i+contents.length()-1;
                }
            }
            openPanel.open(e.p,openPosition);
            return;
        }
        if(e.name.equalsIgnoreCase("close=")) {
            e.commandTagUsed();
            //closes specific panel positions
            PanelPosition position = PanelPosition.valueOf(e.args[0]);
            if(position == PanelPosition.Middle && ctx.openPanels.hasPanelOpen(e.p.getName(),position)){
                ctx.openPanels.closePanelForLoader(e.p.getName(),PanelPosition.Middle);
            }else if(position == PanelPosition.Bottom && ctx.openPanels.hasPanelOpen(e.p.getName(),position)){
                ctx.openPanels.closePanelForLoader(e.p.getName(),PanelPosition.Bottom);
            }else if(position == PanelPosition.Top && ctx.openPanels.hasPanelOpen(e.p.getName(),position)){
                //closing top closes all
                ctx.commandRunner.runCommand(e.panel,e.pos,e.p,"cpc");
            }
            return;
        }
        if(e.name.equalsIgnoreCase("title=")) {
            e.commandTagUsed();
            //added into the 1.11 API
            //will send a title to the player title= <player> <fadeIn> <stay> <fadeOut>
            if(e.args.length >= 5){
                Player p = Bukkit.getPlayer(e.args[0]);
                StringBuilder message = new StringBuilder();
                for(int i = 4; i < e.args.length; i++){
                    message.append(e.args[i]).append(" ");
                }
                message.deleteCharAt(message.length()-1);
                String title;
                String subtitle = "";
                if(message.toString().contains("/n/")) {
                    title = ctx.tex.placeholders(e.panel, e.pos, e.p, message.toString().split("/n/")[0]);
                    subtitle = ctx.tex.placeholders(e.panel, e.pos, e.p, message.toString().split("/n/")[1]);
                }else{
                    title = ctx.tex.placeholders(e.panel, e.pos, e.p, message.toString().trim());
                }
                try{
                    p.sendTitle(title, subtitle, Integer.parseInt(e.args[1]), Integer.parseInt(e.args[2]), Integer.parseInt(e.args[3]));
                }catch(Exception ex) {
                    ctx.debug.send(ex, e.p, ctx);
                }
            }
            return;
        }
        if(e.name.equalsIgnoreCase("teleport=")) {
            e.commandTagUsed();
            float x, y, z, yaw = 0, pitch = 0;
            Player teleportedPlayer = e.p;
            World teleportedWorld = e.p.getWorld();

            try {
                x = Float.parseFloat(e.args[0]);
                y = Float.parseFloat(e.args[1]);
                z = Float.parseFloat(e.args[2]);
                for(String val : e.args) {
                    if(val.startsWith("world:")) {
                        teleportedWorld = Bukkit.getWorld(val.substring(6));
                        continue;
                    }
                    if(val.startsWith("yaw:")) {
                        yaw = Float.parseFloat(val.substring(4));
                        continue;
                    }
                    if(val.startsWith("pitch:")) {
                        pitch = Float.parseFloat(val.substring(6));
                        continue;
                    }
                    if(val.startsWith("player:")) {
                        teleportedPlayer = Bukkit.getPlayer(val.substring(7));
                    }
                }
                teleportedPlayer.teleport(new Location(teleportedWorld, x, y, z, yaw, pitch));
            } catch (Exception tpe) {
                ctx.debug.send(tpe,e.p, ctx);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("delay=")) {
            e.commandTagUsed();
            //if player uses op= it will perform command as op
            final int delayTicks = Integer.parseInt(e.args[0]);
            String finalCommand = String.join(" ",e.args).replaceFirst(e.args[0],"").trim();
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        ctx.commandRunner.runCommand(e.panel,e.pos, e.p, finalCommand);
                    } catch (Exception ex) {
                        //if there are any errors, cancel so that it doesn't loop errors
                        ctx.debug.send(ex, e.p, ctx);
                        this.cancel();
                    }
                    this.cancel();
                }
            }.runTaskTimer(ctx.plugin, delayTicks, 1); //20 ticks == 1 second
        }
        if(e.name.equalsIgnoreCase("eval-delay=")) {
            //Eval delay is used to check if a placeholder equals the same after a delay.
            //Format eval-delay= 20 %hello_world% console= give %cp-player-name% diamond 1
            e.commandTagUsed();
            //if player uses op= it will perform command as op
            final int delayTicks = Integer.parseInt(e.args[0]);
            final String staticValue = e.raw[1];
            final String parsedValue = ctx.tex.placeholders(e.panel, e.pos, e.p, e.args[1].trim());
            String finalCommand = String.join(" ",e.args).replaceFirst(e.args[0],"").replaceFirst(e.args[1],"").trim();
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        //If old parsed value does not equal the new value after delay then stop execute.
                        if(ctx.tex.placeholders(e.panel, e.pos, e.p, staticValue.trim()).equals(parsedValue)){
                            ctx.commandRunner.runCommand(e.panel,e.pos, e.p, finalCommand);
                        }
                    } catch (Exception ex) {
                        //if there are any errors, cancel so that it doesn't loop errors
                        ctx.debug.send(ex, e.p, ctx);
                        this.cancel();
                    }
                    this.cancel();
                }
            }.runTaskTimer(ctx.plugin, delayTicks, 1); //20 ticks == 1 second
        }
    }

    private Character[] convertToCharacterArray(char[] charArray) {
        Character[] characterArray = new Character[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            characterArray[i] = charArray[i];
        }
        return characterArray;
    }
}
