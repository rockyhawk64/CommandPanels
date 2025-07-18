package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import io.papermc.paper.datacomponent.DataComponentTypes;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemModelComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.itemModel() == null) return itemStack;

        NamespacedKey itemModel = NamespacedKey.fromString(ctx.text.parseTextToString(player, item.itemModel()));
        if(itemModel == null) return itemStack;

        itemStack.setData(
                DataComponentTypes.ITEM_MODEL,
                itemModel
        );

        return itemStack;
    }
}