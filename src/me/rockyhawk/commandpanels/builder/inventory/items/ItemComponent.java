package me.rockyhawk.commandpanels.builder.inventory.items;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ItemComponent {
    ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item);
}
