package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemModelComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.itemModel() == null) return itemStack;

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setItemModel(
                NamespacedKey.fromString(ctx.text.parseTextToString(player, item.itemModel()))
        );

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}