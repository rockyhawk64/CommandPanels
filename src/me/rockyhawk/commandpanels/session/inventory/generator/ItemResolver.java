package me.rockyhawk.commandpanels.session.inventory.generator;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface ItemResolver {
    void resolve(ItemStack item, Map<String, Object> itemData);
}
