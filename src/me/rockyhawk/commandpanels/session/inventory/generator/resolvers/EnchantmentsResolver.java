package me.rockyhawk.commandpanels.session.inventory.generator.resolvers;

import me.rockyhawk.commandpanels.session.inventory.generator.ItemResolver;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchantmentsResolver implements ItemResolver {

    @Override
    public void resolve(ItemStack item, Map<String, Object> itemData) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasEnchants()) {
            List<String> enchantList = new ArrayList<>();
            meta.getEnchants().forEach((enchantment, level) -> {
                String name = enchantment.getKey().getKey();
                enchantList.add(name + " " + level);
            });
            itemData.put("enchantments", enchantList);
        }
    }
}