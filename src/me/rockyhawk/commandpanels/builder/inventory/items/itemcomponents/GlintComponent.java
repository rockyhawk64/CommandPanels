package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import io.papermc.paper.datacomponent.DataComponentTypes;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GlintComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if (item.glintOverride() == null) return itemStack;

        boolean glint = Boolean.parseBoolean(
                ctx.text.parseTextToString(player, item.glintOverride())
        );

        itemStack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint);
        return itemStack;
    }
}