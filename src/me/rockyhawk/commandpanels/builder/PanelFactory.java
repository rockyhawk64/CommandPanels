package me.rockyhawk.commandpanels.builder;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class PanelFactory {
    private final Context ctx;

    public PanelFactory(Context ctx) {
        this.ctx = ctx;
    }

    public Inventory createInventory(Panel panel, Player p, PanelPosition position, int animateValue) {
        ConfigurationSection config = panel.getConfig();
        if (position == PanelPosition.Top) {
            TitleHandler titleHandler = new TitleHandler();
            String title = titleHandler.getTitle(ctx, panel, p, position, animateValue);
            String rows = config.getString("rows");

            if (rows.matches("\\d+")) {
                return Bukkit.createInventory(p, Integer.parseInt(rows) * 9, title);
            } else {
                return Bukkit.createInventory(p, InventoryType.valueOf(rows), title);
            }
        } else {
            Inventory inv = p.getInventory();
            return inv;
        }
    }
}