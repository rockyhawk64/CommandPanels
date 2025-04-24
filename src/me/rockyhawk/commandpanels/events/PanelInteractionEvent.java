package me.rockyhawk.commandpanels.events;

import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PanelInteractionEvent extends Event {

    private final Player p;
    private final String args;
    private final Panel panel;

    public PanelInteractionEvent(Player player, String message, Panel panel1) {
        this.p = player;
        this.args = message;
        this.panel = panel1;
    }

    public Player getPlayer(){
        return this.p;
    }

    public Panel getPanel(){
        return this.panel;
    }

    public String getMessage(){
        return this.args;
    }

    private static final HandlerList HANDLERS = new HandlerList();
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
