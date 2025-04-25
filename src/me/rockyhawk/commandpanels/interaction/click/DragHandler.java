package me.rockyhawk.commandpanels.interaction.click;

import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;

public class DragHandler {
    private final InteractionHandler handler;

    protected DragHandler(InteractionHandler handler) {
        this.handler = handler;
    }

    protected void handle(InventoryDragEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (!handler.ctx.openPanels.hasPanelOpen(p.getName(), PanelPosition.Top)) return;
        if (e.getInventory().getType() != InventoryType.PLAYER) {
            e.setCancelled(true);
        }
    }
}