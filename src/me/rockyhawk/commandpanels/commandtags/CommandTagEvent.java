package me.rockyhawk.commandpanels.commandtags;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CommandTagEvent extends Event {

    public final Player p;
    public final Panel panel;
    public String[] raw;
    public String[] args;
    public String name;
    public boolean commandTagUsed = false;

    public CommandTagEvent(CommandPanels plugin,  Panel panel1, Player player, String rawCommand1) {
        this.p = player;
        this.panel = panel1;

        String[] split = rawCommand1.split(" ", 2);
        if(split.length == 1){
            split = new String[]{split[0],""};
        }
        if(split[1].contains("%cp-player-input%")){
            //set command to cpc and then use full command for input
            this.name = "cpc";
            return;
        }

        this.name = split[0].trim();
        this.raw = split[1].trim().split("\\s");
        this.args = plugin.tex.papi(panel1,player,split[1].trim()).split("\\s");
    }

    public void commandTagUsed(){
        commandTagUsed = true;
    }

    private static final HandlerList HANDLERS = new HandlerList();
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
