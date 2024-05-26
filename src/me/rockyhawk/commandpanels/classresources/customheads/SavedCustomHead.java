package me.rockyhawk.commandpanels.classresources.customheads;

import org.bukkit.inventory.ItemStack;

public class SavedCustomHead {
    public String base64;
    public String playerName = null;
    public ItemStack headItem;

    public SavedCustomHead(ItemStack head, String base64value) {
        base64 = base64value;
        headItem = head;
    }

    public SavedCustomHead(ItemStack head, String base64value, String player) {
        playerName = player;
        base64 = base64value;
        headItem = head;
    }
}
