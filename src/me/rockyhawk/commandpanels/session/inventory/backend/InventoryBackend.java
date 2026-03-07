package me.rockyhawk.commandpanels.session.inventory.backend;

import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import me.rockyhawk.commandpanels.session.inventory.render.InventoryRenderSnapshot;
import org.bukkit.entity.Player;

public interface InventoryBackend {
    InventoryBackendType type();

    void applySnapshot(Player player, InventoryPanel panel, InventoryRenderSnapshot snapshot);

    boolean isViewing(Player player, InventoryPanel panel);
}
