package me.rockyhawk.commandpanels.session.inventory;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.InventoryPanelBuilder;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemBuilder;
import me.rockyhawk.commandpanels.session.PanelUpdater;
import me.rockyhawk.commandpanels.session.PanelSession;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class InventoryPanelUpdater implements PanelUpdater {

    private BukkitRunnable task;

    @Override
    public void start(Context ctx, PanelSession session) {
        if (!(session.getPanel() instanceof InventoryPanel panel)) return;

        // If restarted, stop current event
        stop();

        InventoryPanelBuilder panelBuilder = new InventoryPanelBuilder(ctx, session.getPlayer());
        ItemBuilder builder = new ItemBuilder(ctx, panelBuilder);

        int updateDelay = 20;
        if(panel.getUpdateDelay().matches("\\d+")){
            // Update delay value is a number
            updateDelay = Integer.parseInt(panel.getUpdateDelay());
        }

        // If update delay is 0 then do not run the updater
        if(updateDelay == 0){
            this.task = null;
            return;
        }

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                Inventory inv = session.getPlayer().getOpenInventory().getTopInventory();
                NamespacedKey itemIdKey = new NamespacedKey(ctx.plugin, "item_id");
                NamespacedKey baseIdKey = new NamespacedKey(ctx.plugin, "base_item_id");
                NamespacedKey fillItem = new NamespacedKey(ctx.plugin, "fill_item");

                for (int slot = 0; slot < inv.getSize(); slot++) {
                    ItemStack item = inv.getItem(slot);
                    if (item == null || item.getType().isAir()) continue;

                    ItemMeta meta = item.getItemMeta();
                    if (meta == null) continue;

                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    if (!container.has(itemIdKey, PersistentDataType.STRING) ||
                            container.has(fillItem, PersistentDataType.STRING)) continue;
                    String itemId = container.get(itemIdKey, PersistentDataType.STRING);
                    String baseItemId = container.get(baseIdKey, PersistentDataType.STRING);

                    PanelItem panelItem = panel.getItems().get(itemId);
                    if(!panelItem.animate().isEmpty()){
                        // If there is an animation
                        PanelItem animateItem = panel.getItems().get(panelItem.animate());
                        if(animateItem != null) panelItem = animateItem;

                    }else if(!baseItemId.equals(itemId)){
                        // If baseItemId is different from itemId with no animation, change back to base item
                        panelItem = panel.getItems().get(baseItemId);

                    }

                    // Build the new item
                    ItemStack newItem = builder.buildItem(panel, panelItem);

                    // Update base item to original base item
                    ItemMeta newMeta = newItem.getItemMeta();
                    newMeta.getPersistentDataContainer().set(baseIdKey, PersistentDataType.STRING, baseItemId);
                    newItem.setItemMeta(newMeta);

                    inv.setItem(slot, newItem);
                }
            }
        };

        task.runTaskTimer(ctx.plugin, updateDelay, updateDelay);
    }

    @Override
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
