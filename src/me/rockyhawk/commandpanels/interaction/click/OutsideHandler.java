package me.rockyhawk.commandpanels.interaction.click;

import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.List;

public class OutsideHandler {
    private final InteractionHandler handler;

    protected OutsideHandler(InteractionHandler handler) {
        this.handler = handler;
    }

    protected void handle(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        boolean panelOpen = handler.ctx.openPanels.hasPanelOpen(p.getName(), PanelPosition.Top);
        boolean isOutsideClick = e.getSlotType() == InventoryType.SlotType.OUTSIDE;

        if ((panelOpen || e.getClick() == ClickType.DOUBLE_CLICK) && isOutsideClick) {
            Panel panel = handler.ctx.openPanels.getOpenPanel(p.getName(), PanelPosition.Top);
            if (panel != null && panel.getConfig().contains("outside-commands")) {
                runCommands(panel.getConfig().getStringList("outside-commands"), panel, p, e);
                return;
            }
        }

        if (isOutsideClick && !panelOpen && handler.ctx.configHandler.config.contains("config.outside-commands")) {
            runCommands(handler.ctx.configHandler.config.getStringList("config.outside-commands"), null, p, e);
        }
    }

    private void runCommands(List<String> commands, Panel panel, Player player, InventoryClickEvent e) {
        try {
            handler.ctx.commands.runCommands(panel, PanelPosition.Top, player, commands, e.getClick());
        } catch (Exception ex) {
            handler.ctx.debug.send(ex, player, handler.ctx);
        }
    }
}