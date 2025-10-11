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

import java.util.HashMap;
import java.util.Map;

public class InventoryPanelUpdater {

    private ScheduledTask checkTask;
    private ScheduledTask updateTask;

    // List of permission states for observed permissions
    private final Map<String, Boolean> lastObservedPermStates = new HashMap<>();

    /**
     * Panel updater will maintain itself with a checkTask that will end the updater
     * If it finds the panel has been closed it will end the updater tasks
     */

    public void start(Context ctx, Player p, InventoryPanel panel) {
        // Stop existing tasks if any
        stop();

        // Determine update delay
        int updateDelay = 20;
        if (panel.getUpdateDelay().matches("\\d+")) {
            updateDelay = Integer.parseInt(panel.getUpdateDelay());
        }

        // If update delay is 0 then do not run the updater
        if (updateDelay == 0) {
            this.updateTask = null;
            return;
        }

        InventoryPanelBuilder panelBuilder = new InventoryPanelBuilder(ctx, p);
        ItemBuilder builder = new ItemBuilder(ctx, panelBuilder);

        // Main update task
        this.updateTask = Bukkit.getRegionScheduler().runAtFixedRate(
                ctx.plugin,
                p.getLocation(),
                (scheduledTask) -> {
                    Inventory inv = p.getOpenInventory().getTopInventory();
                    InventoryHolder holder = inv.getHolder();
                    if (!(holder instanceof InventoryPanel) || holder != panel) {
                        stop();
                        return;
                    }

                    NamespacedKey itemIdKey = new NamespacedKey(ctx.plugin, "item_id");
                    NamespacedKey baseIdKey = new NamespacedKey(ctx.plugin, "base_item_id");
                    NamespacedKey fillItem = new NamespacedKey(ctx.plugin, "fill_item");

                    // Loop through items in the panel and update their state
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

        // Fast heartbeat check task, should run frequently
        this.checkTask = Bukkit.getRegionScheduler().runAtFixedRate(
                ctx.plugin,
                p.getLocation(),
                (scheduledTask) -> {
                    Inventory inv = p.getOpenInventory().getTopInventory();
                    InventoryHolder holder = inv.getHolder();

                    if (!(holder instanceof InventoryPanel) || holder != panel) {
                        stop();
                        return;
                    }

                    // Do a refresh if an observed perms state changes
                    for (String node : panel.getObservedPerms()) {
                        boolean currentState = p.hasPermission(node);
                        Boolean previousState = lastObservedPermStates.get(node);
                        lastObservedPermStates.put(node, currentState);
                        if (previousState != null && previousState != currentState) {
                            panel.open(ctx, p, false);
                            return;
                        }
                    }
                },
                2,
                2
        );
    }

    public void stop() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
}
