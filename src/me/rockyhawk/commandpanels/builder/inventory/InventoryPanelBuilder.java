package me.rockyhawk.commandpanels.builder.inventory;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.SessionManager;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class InventoryPanelBuilder extends PanelBuilder {

    private final PanelFactory panelFactory;

    public InventoryPanelBuilder(Context ctx, Player player) {
        super(ctx, player);
        this.panelFactory = new PanelFactory(ctx, this);
    }

    @Override
    public void open(Panel panel, SessionManager.PanelOpenType openType){
        if (!(panel instanceof InventoryPanel)) {
            throw new IllegalArgumentException("Expected InventoryPanel, got " + panel.getClass());
        }
        Inventory panelInv = panelFactory.createInventory((InventoryPanel) panel, this.getPlayer());
        if(panelInv.isEmpty()) {
            ctx.text.sendError(this.getPlayer(), Message.PANEL_NO_ITEMS);
            return;
        }
        this.getPlayer().openInventory(panelInv);
        ctx.session.updateSession(this.getPlayer(), panel, openType);
    }
}