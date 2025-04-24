package me.rockyhawk.commandpanels.items.customheads;

import org.bukkit.inventory.ItemStack;

public interface CustomHeadProvider {
    ItemStack getCustomHead(String base64);
    ItemStack getPlayerHead(String playerName);
}