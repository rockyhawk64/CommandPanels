package me.rockyhawk.commandpanels.builder.inventory.items;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface MaterialComponent {
    boolean isCorrectTag(String materialTag);
    ItemStack createItem(Context ctx, String args, Player player, PanelItem item);
}