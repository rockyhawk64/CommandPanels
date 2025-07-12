package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

public class PotionColorComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.potionColor() == null) return itemStack;

        String[] rgb = ctx.text.parseTextToString(player, item.potionColor()).split(",");
        Color color = Color.fromRGB(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();

        potionMeta.setColor(color);
        itemStack.setItemMeta(potionMeta);
        return itemStack;
    }
}