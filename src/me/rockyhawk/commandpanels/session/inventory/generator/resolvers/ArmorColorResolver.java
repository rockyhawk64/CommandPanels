package me.rockyhawk.commandpanels.session.inventory.generator.resolvers;

import me.rockyhawk.commandpanels.session.inventory.generator.ItemResolver;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Map;

public class ArmorColorResolver implements ItemResolver {

    @Override
    public void resolve(ItemStack item, Map<String, Object> itemData) {
        if (!(item.getItemMeta() instanceof LeatherArmorMeta meta)) return;

        Color color = meta.getColor();
        itemData.put("leather-color", color.getRed() + "," + color.getGreen() + "," + color.getBlue());
    }
}