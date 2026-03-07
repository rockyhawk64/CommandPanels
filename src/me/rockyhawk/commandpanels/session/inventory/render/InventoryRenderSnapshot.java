package me.rockyhawk.commandpanels.session.inventory.render;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record InventoryRenderSnapshot(
        Component title,
        int size,
        List<ItemStack> topItems,
        Map<Integer, String> actionSlots
) {
    public InventoryRenderSnapshot {
        topItems = Collections.unmodifiableList(new ArrayList<>(topItems));
        actionSlots = Map.copyOf(actionSlots);
    }

    public ItemStack itemAt(int slot) {
        return slot >= 0 && slot < topItems.size() ? topItems.get(slot) : null;
    }
}
