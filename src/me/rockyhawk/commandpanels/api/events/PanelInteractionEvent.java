package me.rockyhawk.commandpanels.api.events;

import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PanelInteractionEvent extends Event {

    private final Player p;
    private final int slot;
    private final Panel panel;
    private final PanelPosition position;
    private boolean isCancelled;

    public PanelInteractionEvent(Player player, int slot, Panel panel1, PanelPosition position) {
        this.p = player;
        this.slot = slot;
        this.panel = panel1;
        this.position = position;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public Player getPlayer(){
        return this.p;
    }

    public PanelPosition getPosition(){
        return this.position;
    }

    public Panel getPanel(){
        return this.panel;
    }

    public int getSlot(){
        return this.slot;
    }

    private static final HandlerList HANDLERS = new HandlerList();
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
