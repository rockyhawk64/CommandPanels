package me.rockyhawk.commandpanels.session.inventory;

import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.InventoryPanelBuilder;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.joml.Random;

public class InventoryPanelUpdater {

    private ScheduledTask task;

    public void start(Context ctx, Player p, InventoryPanel panel) {
        // If restarted, stop current event
        stop();

        InventoryPanelBuilder panelBuilder = new InventoryPanelBuilder(ctx, p);
        ItemBuilder builder = new ItemBuilder(ctx, panelBuilder);

        int updateDelay = 20;
        if (panel.getUpdateDelay().matches("\\d+")) {
            // Update delay value is a number
            updateDelay = Integer.parseInt(panel.getUpdateDelay());
        }

        // If update delay is 0 then do not run the updater
        if (updateDelay == 0) {
            this.task = null;
            return;
        }

        // Schedule repeating GUI update task on the player's region
        this.task = Bukkit.getRegionScheduler().runAtFixedRate(
                ctx.plugin,
                p.getLocation(),
                (scheduledTask) -> {
                    Inventory inv = p.getOpenInventory().getTopInventory();

                    InventoryHolder holder = inv.getHolder();
                    if (!(holder instanceof InventoryPanel) || holder != panel) {
                        stop();
                        return;
                    }
                    p.sendMessage(panel.getName() + " " + Random.newSeed());

                    NamespacedKey itemIdKey = new NamespacedKey(ctx.plugin, "item_id");
                    NamespacedKey baseIdKey = new NamespacedKey(ctx.plugin, "base_item_id");
                    NamespacedKey fillItem = new NamespacedKey(ctx.plugin, "fill_item");

                    for (int slot = 0; slot < inv.getSize(); slot++) {
                        ItemStack item = inv.getItem(slot);
                        if (item == null || item.getType().isAir()) continue;

                        PersistentDataContainerView container = item.getPersistentDataContainer();
                        if (!container.has(itemIdKey, PersistentDataType.STRING) ||
                                container.has(fillItem, PersistentDataType.STRING)) continue;

                        String itemId = container.get(itemIdKey, PersistentDataType.STRING);
                        String baseItemId = container.get(baseIdKey, PersistentDataType.STRING);

                        PanelItem panelItem = panel.getItems().get(itemId);
                        if (!panelItem.animate().isEmpty()) {
                            // If there is an animation
                            PanelItem animateItem = panel.getItems().get(panelItem.animate());
                            if (animateItem != null) panelItem = animateItem;

                        } else if (!baseItemId.equals(itemId)) {
                            // If baseItemId is different from itemId with no animation, change back to base item
                            panelItem = panel.getItems().get(baseItemId);
                        }

                        // Build the new item
                        ItemStack newItem = builder.buildItem(panel, panelItem);

                        // Update base item to original base item
                        newItem.editPersistentDataContainer(c ->
                                c.set(baseIdKey, PersistentDataType.STRING, baseItemId));

                        inv.setItem(slot, newItem);
                    }
                },
                updateDelay,
                updateDelay
        );
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
