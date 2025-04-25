package me.rockyhawk.commandpanels.interaction.click;

import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;

public class ClickHandler {
    private final InteractionHandler handler;
    private final ClickSlotResolver slotResolver;
    private final ClickActionExecutor actionExecutor;

    protected ClickHandler(InteractionHandler handler) {
        this.handler = handler;
        this.slotResolver = new ClickSlotResolver(handler);
        this.actionExecutor = new ClickActionExecutor(handler);
    }

    protected void handle(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        int clickedSlot = e.getSlot();

        if (!handler.ctx.openPanels.hasPanelOpen(p.getName(), PanelPosition.Top) || e.getClick() == ClickType.DOUBLE_CLICK) return;
        Panel panel = handler.ctx.openPanels.getOpenPanel(p.getName(), PanelPosition.Top);

        if(e.getSlotType() == InventoryType.SlotType.OUTSIDE) return;

        PanelPosition position = slotResolver.resolveSlotPosition(e, panel, p, clickedSlot);
        if(position == PanelPosition.Middle) clickedSlot -= 9;
        if (position == null) {
            e.setCancelled(true);
            return;
        }

        panel = handler.ctx.openPanels.getOpenPanel(p.getName(), position);
        String foundSlot = slotResolver.resolveClickedItem(panel, p, clickedSlot, position);

        if (foundSlot == null) {
            e.setCancelled(true);
            return;
        }

        actionExecutor.execute(panel, p, e, foundSlot, position);
    }
}