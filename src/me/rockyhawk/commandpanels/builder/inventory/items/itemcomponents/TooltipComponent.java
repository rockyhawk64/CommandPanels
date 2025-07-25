package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import io.papermc.paper.datacomponent.DataComponentTypes;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TooltipComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.tooltipStyle() == null) return itemStack;

        NamespacedKey tooltipStyle = NamespacedKey.fromString(ctx.text.parseTextToString(player, item.tooltipStyle()));

        itemStack.setData(DataComponentTypes.TOOLTIP_STYLE,
                tooltipStyle);

        return itemStack;
    }
}