package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantedComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.enchantments().isEmpty()) return itemStack;

        ItemMeta EnchantMeta;
        EnchantMeta = itemStack.getItemMeta();

        for(String enchantment : item.enchantments()){
            String[] enchant = ctx.text.parseTextToString(player, enchantment)
                    .toLowerCase()
                    .split("\\s");
            NamespacedKey key = enchant[0].contains(":") ?
                    NamespacedKey.fromString(enchant[0]) :
                    NamespacedKey.minecraft(enchant[0]);

            EnchantMeta.addEnchant(Registry.ENCHANTMENT.get(key), Integer.parseInt(enchant[1]), true);
        }
        itemStack.setItemMeta(EnchantMeta);

        return itemStack;
    }
}