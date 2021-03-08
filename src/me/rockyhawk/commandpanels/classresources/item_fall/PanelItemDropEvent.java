package me.rockyhawk.commandpanels.classresources.item_fall;

import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PanelItemDropEvent extends Event implements Cancellable {

    private boolean isCancelled;
    private final Player p;
    private final Panel panel;
    private final ItemStack item;

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public PanelItemDropEvent(Player player, Panel panel, ItemStack drop) {
        this.p = player;
        this.panel = panel;
        this.item = drop;
    }

    public Player getPlayer(){
        return this.p;
    }

    public ItemStack getItem(){
        return this.item;
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
