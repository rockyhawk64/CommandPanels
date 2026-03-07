package me.rockyhawk.commandpanels.session.inventory.render;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.InventoryPanelBuilder;
import me.rockyhawk.commandpanels.builder.inventory.PanelFactory;
import me.rockyhawk.commandpanels.builder.inventory.TitleHandler;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryPanelRenderer {
    private final Context ctx;

    public InventoryPanelRenderer(Context ctx) {
        this.ctx = ctx;
    }

    public InventoryRenderSnapshot render(Player player, InventoryPanel panel) {
        Component title = new TitleHandler().getTitle(ctx, panel, player);
        Inventory inventory = new PanelFactory(ctx, new InventoryPanelBuilder(ctx, player)).createInventory(panel, player);

        NamespacedKey baseItemId = new NamespacedKey(ctx.plugin, "base_item_id");
        NamespacedKey filler = new NamespacedKey(ctx.plugin, "fill_item");

        List<ItemStack> items = new ArrayList<>(inventory.getSize());
        Map<Integer, String> actionSlots = new HashMap<>();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            items.add(item == null ? null : item.clone());

            if (item == null || !item.hasItemMeta()) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(filler, PersistentDataType.STRING)) {
                continue;
            }

            String itemId = container.get(baseItemId, PersistentDataType.STRING);
            if (itemId != null) {
                actionSlots.put(slot, itemId);
            }
        }

        return new InventoryRenderSnapshot(title, inventory.getSize(), items, actionSlots);
    }
}
