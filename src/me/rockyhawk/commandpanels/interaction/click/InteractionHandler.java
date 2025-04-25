package me.rockyhawk.commandpanels.interaction.click;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class InteractionHandler implements Listener {
    protected final Context ctx;
    private final DragHandler dragHandler;
    private final OutsideHandler outsideHandler;
    private final ClickHandler clickHandler;

    public InteractionHandler(Context ctx) {
        this.ctx = ctx;
        this.dragHandler = new DragHandler(this);
        this.outsideHandler = new OutsideHandler(this);
        this.clickHandler = new ClickHandler(this);
    }

    @EventHandler
    public void handleDrag(InventoryDragEvent e) {
        dragHandler.handle(e);
    }

    @EventHandler
    public void handleClick(InventoryClickEvent e) {
        clickHandler.handle(e);
        outsideHandler.handle(e);
    }
}