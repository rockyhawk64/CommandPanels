package me.rockyhawk.commandpanels.api;

import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

public class PanelClosedEvent extends Event{

    private final Player p;
    private final Panel panel;
    private final PanelPosition pos;

    public PanelClosedEvent(Player player, Panel panel, PanelPosition position) {
        this.p = player;
        this.panel = panel;
        this.pos = position;
    }

    public Player getPlayer(){
        return this.p;
    }

    public PanelPosition getPosition(){
        return this.pos;
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
