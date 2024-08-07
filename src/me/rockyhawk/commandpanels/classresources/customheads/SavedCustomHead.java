package me.rockyhawk.commandpanels.classresources.customheads;

import org.bukkit.inventory.ItemStack;

public class SavedCustomHead {
    public String base64;
    public ItemStack headItem;
    public boolean isValid; // true if the head was successfully fetched, false otherwise
    public long lastAttempt; // timestamp of the last attempt

    public SavedCustomHead(ItemStack head, String base64value, boolean isValidAttempt) {
        base64 = base64value;
        headItem = head;
        isValid = isValidAttempt;
        lastAttempt = System.currentTimeMillis();
    }
}
