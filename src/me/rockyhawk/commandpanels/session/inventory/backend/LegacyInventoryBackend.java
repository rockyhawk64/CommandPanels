package me.rockyhawk.commandpanels.session.inventory.backend;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import me.rockyhawk.commandpanels.builder.inventory.InventoryPanelBuilder;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import me.rockyhawk.commandpanels.session.inventory.render.InventoryRenderSnapshot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LegacyInventoryBackend implements InventoryBackend {
    private final Context ctx;

    public LegacyInventoryBackend(Context ctx) {
        this.ctx = ctx;
    }

    public void open(Player player, InventoryPanel panel) {
        PanelBuilder builder = new InventoryPanelBuilder(ctx, player);
        builder.open(panel);
    }

    @Override
    public InventoryBackendType type() {
        return InventoryBackendType.LEGACY;
    }

    @Override
    public void applySnapshot(Player player, InventoryPanel panel, InventoryRenderSnapshot snapshot) {
        if (!isViewing(player, panel)) {
            return;
        }

        Inventory inventory = player.getOpenInventory().getTopInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = snapshot.itemAt(slot);
            inventory.setItem(slot, item == null ? null : item.clone());
        }
    }

    @Override
    public boolean isViewing(Player player, InventoryPanel panel) {
        return player.getOpenInventory().getTopInventory().getHolder() instanceof InventoryPanel current
                && current == panel;
    }
}
