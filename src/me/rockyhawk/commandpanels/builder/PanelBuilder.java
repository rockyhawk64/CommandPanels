package me.rockyhawk.commandpanels.builder;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class PanelBuilder {
    protected final Context ctx;
    protected final PanelFactory factory;
    protected final SlotManager slotManager;
    protected final ItemPlacer itemPlacer;
    protected boolean isFirstBuild;

    public PanelBuilder(Context ctx) {
        this.ctx = ctx;
        this.factory = new PanelFactory(ctx);
        this.slotManager = new SlotManager(this);
        this.itemPlacer = new ItemPlacer(this);
    }

    public void openInv(Panel panel, Player p, PanelPosition position, int animateValue) {
        isFirstBuild = true;
        Inventory inv = buildInv(panel, p, position, animateValue);
        if (position == PanelPosition.Top) {
            p.openInventory(inv);
        }
        ctx.openPanels.openPanelForLoader(p.getName(), panel, position);
    }

    public void refreshInv(Panel panel, Player p, PanelPosition position, int animateValue) {
        isFirstBuild = false;
        Inventory inv = buildInv(panel, p, position, animateValue);
        if (ctx.version.isAtLeast("1.21.5") && position == PanelPosition.Top) {
            TitleHandler title = new TitleHandler();
            p.getOpenInventory().setTitle(title.getTitle(ctx, panel, p, position, animateValue));
        }
        if (position == PanelPosition.Top) {
            slotManager.setStorageContents(p, slotManager.getStorageContents(inv));
        }
    }

    public Inventory getInv(Panel panel, Player p, PanelPosition position, int animateValue) {
        isFirstBuild = true;
        return buildInv(panel, p, position, animateValue);
    }

    private Inventory buildInv(Panel panel, Player p, PanelPosition position, int animateValue) {
        Inventory inv = factory.createInventory(panel, p, position, animateValue);
        slotManager.takenSlots.clear();
        slotManager.populateSlots(panel, p, position, animateValue, inv);
        itemPlacer.placeEmptyItems(panel, p, position, inv);
        return inv;
    }
}