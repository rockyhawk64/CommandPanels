package me.rockyhawk.commandpanels.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PanelCommandEvent extends Event {

    private Player p;
    private String args;

    public PanelCommandEvent(Player player, String message) {
        this.p = player;
        this.args = message;
    }

    public Player getPlayer(){
        return this.p;
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
