package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TooltipComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.tooltipStyle() == null) return itemStack;

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setTooltipStyle(NamespacedKey.fromString(ctx.text.parseTextToString(player, item.tooltipStyle())));
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}