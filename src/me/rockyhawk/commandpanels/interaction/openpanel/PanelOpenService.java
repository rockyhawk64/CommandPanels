package me.rockyhawk.commandpanels.interaction.openpanel;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import me.rockyhawk.commandpanels.session.inventory.backend.InventoryCloseReason;
import org.bukkit.entity.Player;

public class PanelOpenService {
    private final Context ctx;

    public PanelOpenService(Context ctx) {
        this.ctx = ctx;
    }

    public void scheduleOpenPanel(Panel panel, Player player, boolean isNewPanelSession, boolean sendDeniedMessage) {
        player.getScheduler().run(ctx.plugin, task ->
                openPanel(panel, player, isNewPanelSession, sendDeniedMessage), null);
    }

    public boolean openPanel(Panel panel, Player player, boolean isNewPanelSession, boolean sendDeniedMessage) {
        if (!panel.passesConditions(player, ctx)) {
            if (sendDeniedMessage) {
                ctx.text.sendError(player, Message.COMMAND_NO_PERMISSION);
            }

            if (!isNewPanelSession && panel instanceof InventoryPanel inventoryPanel
                    && ctx.inventoryPanels.isViewingPanel(player, inventoryPanel)) {
                ctx.inventoryPanels.closeActiveView(player, InventoryCloseReason.CONDITION_FAILED, true);
            }
            return false;
        }

        if (isNewPanelSession && !panel.canOpen(player, ctx)) {
            return false;
        }

        if (!(panel instanceof InventoryPanel)) {
            ctx.inventoryPanels.closePacketSession(player, InventoryCloseReason.REPLACED, false);
        }

        panel.open(ctx, player, isNewPanelSession);
        return true;
    }
}
