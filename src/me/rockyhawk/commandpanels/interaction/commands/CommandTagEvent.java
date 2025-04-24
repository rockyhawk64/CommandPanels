package me.rockyhawk.commandpanels.interaction.commands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CommandTagEvent extends Event {

    public final Player p;
    public final Panel panel;
    public String[] raw;
    public String[] args;
    public String name;
    public PanelPosition pos;
    public boolean commandTagUsed = false;

    public CommandTagEvent(Context ctx, Panel panel1, PanelPosition position, Player player, String rawCommand1) {
        this.p = player;
        this.panel = panel1;
        this.pos = position;

        //do nopapi= tag which will stop PlaceholderAPI placeholders from executing
        boolean doApiPlaceholders = true;
        if(rawCommand1.startsWith("nopapi= ")){
            rawCommand1 = rawCommand1.replace("nopapi= ","");
            doApiPlaceholders = false;
        }

        String[] split = rawCommand1.split(" ", 2);
        if(split.length == 1){
            split = new String[]{split[0],""};
        }

        this.name = split[0].trim();
        this.raw = split[1].trim().split("\\s");
        if(doApiPlaceholders) {
            this.args = ctx.text.attachPlaceholders(panel1,pos, player, split[1].trim()).split("\\s");
        }else{
            this.args = ctx.placeholders.setPlaceholders(panel, pos, p,split[1].trim(),false).split("\\s");
            this.args = ctx.placeholders.setPlaceholders(panel, pos, p,split[1].trim(),true).split("\\s");
        }
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
