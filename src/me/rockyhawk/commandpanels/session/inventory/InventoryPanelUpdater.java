package me.rockyhawk.commandpanels.session.inventory;

import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.InventoryPanelBuilder;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class InventoryPanelUpdater {

    private ScheduledTask heartbeatTask;
    private ScheduledTask updateTask;

    private final Map<String, Boolean> lastObservedPermStates = new HashMap<>();

    public void start(Context ctx, Player p, InventoryPanel panel) {
        stop(); // always clean slate

        startHeartbeat(ctx, p, panel);

        int updateDelay = parseUpdateDelay(panel.getUpdateDelay());
        if (updateDelay > 0) {
            startUpdater(ctx, p, panel, updateDelay);
        }
    }

    private void startHeartbeat(Context ctx, Player p, InventoryPanel panel) {
        final boolean isUsingPermObserver = ctx.fileHandler.config.getBoolean("permission-observer");

        heartbeatTask = p.getScheduler().runAtFixedRate(
                ctx.plugin,
                (task) -> {
                    Inventory inv = p.getOpenInventory().getTopInventory();
                    InventoryHolder holder = inv.getHolder();

                    // Stop everything if the panel is closed
                    if (!(holder instanceof InventoryPanel) || holder != panel) {
                        stop();
                        return;
                    }

                    // Handle permission observer
                    if (!isUsingPermObserver) return;
                    for (String node : panel.getObservedPerms()) {
                        boolean current = p.hasPermission(node);
                        Boolean previous = lastObservedPermStates.put(node, current);
                        if (previous != null && previous != current) {
                            panel.open(ctx, p, false);
                            return;
                        }
                    }
                },
                null,
                2,
                2
        );
    }

    private void startUpdater(Context ctx, Player p, InventoryPanel panel, int updateDelay) {
        InventoryPanelBuilder panelBuilder = new InventoryPanelBuilder(ctx, p);
        ItemBuilder builder = new ItemBuilder(ctx, panelBuilder);

        NamespacedKey itemIdKey = new NamespacedKey(ctx.plugin, "item_id");
        NamespacedKey baseIdKey = new NamespacedKey(ctx.plugin, "base_item_id");
        NamespacedKey fillItem = new NamespacedKey(ctx.plugin, "fill_item");

        updateTask = p.getScheduler().runAtFixedRate(
                ctx.plugin,
                (task) -> {
                    Inventory inv = p.getOpenInventory().getTopInventory();
                    InventoryHolder holder = inv.getHolder();
                    if (!(holder instanceof InventoryPanel) || holder != panel) {
                        stopUpdater(); // only stop this task, heartbeat may continue
                        return;
                    }

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
                null,
                updateDelay,
                updateDelay
        );
    }

    private int parseUpdateDelay(String delayStr) {
        if (delayStr != null && delayStr.matches("\\d+")) {
            return Integer.parseInt(delayStr);
        }
        return 20; // default
    }

    public void stop() {
        stopHeartbeat();
        stopUpdater();
    }

    private void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }
    }

    private void stopUpdater() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
}
