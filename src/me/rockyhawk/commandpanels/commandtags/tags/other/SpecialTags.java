package me.rockyhawk.commandpanels.commandtags.tags.other;

import me.rockyhawk.commandpanels.CommandPanels;
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
    CommandPanels plugin;
    public SpecialTags(CommandPanels pl) {
        this.plugin = pl;
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
            for(Panel pane : plugin.panelList){
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
                    String value = plugin.tex.placeholders(e.panel,e.pos,e.p,contents.substring(contents.indexOf(':')+1));
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
            if(position == PanelPosition.Middle && plugin.openPanels.hasPanelOpen(e.p.getName(),position)){
                plugin.openPanels.closePanelForLoader(e.p.getName(),PanelPosition.Middle);
            }else if(position == PanelPosition.Bottom && plugin.openPanels.hasPanelOpen(e.p.getName(),position)){
                plugin.openPanels.closePanelForLoader(e.p.getName(),PanelPosition.Bottom);
            }else if(position == PanelPosition.Top && plugin.openPanels.hasPanelOpen(e.p.getName(),position)){
                //closing top closes all
                plugin.commandTags.runCommand(e.panel,e.pos,e.p,"cpc");
            }
            return;
        }
        if(e.name.equalsIgnoreCase("panel-title=")) {
            e.commandTagUsed();
            //added into the 1.20 API
            //will set the open panel title to the provided string panel-title= <VALUE>
            //AS OF TINYTANK800's pull this only works on static panels!
            if(e.args.length >= 1){
                StringBuilder title = new StringBuilder();
                for(String args : e.args){
                    title.append(args).append(" ");
                }
                e.p.getOpenInventory().setTitle(title.toString());
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
                    title = plugin.tex.placeholders(e.panel, e.pos, e.p, message.toString().split("/n/")[0]);
                    subtitle = plugin.tex.placeholders(e.panel, e.pos, e.p, message.toString().split("/n/")[1]);
                }else{
                    title = plugin.tex.placeholders(e.panel, e.pos, e.p, message.toString().trim());
                }
                try{
                    p.sendTitle(title, subtitle, Integer.parseInt(e.args[1]), Integer.parseInt(e.args[2]), Integer.parseInt(e.args[3]));
                }catch(Exception ex) {
                    plugin.debug(ex, e.p);
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
                plugin.debug(tpe,e.p);
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
                        plugin.commandTags.runCommand(e.panel,e.pos, e.p, finalCommand);
                    } catch (Exception ex) {
                        //if there are any errors, cancel so that it doesn't loop errors
                        plugin.debug(ex, e.p);
                        this.cancel();
                    }
                    this.cancel();
                }
            }.runTaskTimer(plugin, delayTicks, 1); //20 ticks == 1 second
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
