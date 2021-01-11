package me.rockyhawk.commandpanels.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

public class PanelOpenedEvent extends Event implements Cancellable {

    private boolean isCancelled;
    private final Player p;
    private final Panel panel;

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public PanelOpenedEvent(Player player, Panel panel) {
        this.p = player;
        this.panel = panel;
    }

    public Player getPlayer(){
        return this.p;
    }

    public Inventory getInventory(){
        return this.p.getInventory();
    }

    public Panel getPanel(){
        return this.panel;
    }

    private static final HandlerList HANDLERS = new HandlerList();
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
