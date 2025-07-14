package me.rockyhawk.commandpanels.session.inventory.generator.resolvers;

import me.rockyhawk.commandpanels.session.inventory.generator.ItemResolver;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class ItemModelResolver implements ItemResolver {

    @Override
    public void resolve(ItemStack item, Map<String, Object> itemData) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasItemModel()) {
            itemData.put("item-model", meta.getItemModel().toString());
        }
    }
}
