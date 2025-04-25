package me.rockyhawk.commandpanels.items.builder.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.ItemComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class EnchantedComponent implements ItemComponent {

    @Override
    public ItemStack apply(ItemStack item, ConfigurationSection section, Context ctx, Player player, Panel panel, PanelPosition pos, boolean addNBT) {
        if(!section.contains("enchanted")) return item;

        ItemMeta EnchantMeta;
        if(section.isList("enchanted")){
            //if list contains true, hide enchanted and add KNOCKBACK
            EnchantMeta = item.getItemMeta();
            assert EnchantMeta != null;
            for(String enchantment : section.getStringList("enchanted")){
                if(enchantment.equalsIgnoreCase("true")) {
                    EnchantMeta.addEnchant(Enchantment.KNOCKBACK, 1, false);
                    EnchantMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    break;
                }
                String enchant = enchantment.split("\\s")[0].toLowerCase();
                NamespacedKey key = enchant.contains(":") ?
                        NamespacedKey.fromString(enchant) :
                        NamespacedKey.minecraft(enchant);
                EnchantMeta.addEnchant(Objects.requireNonNull(EnchantmentWrapper.getByKey(key)), Integer.parseInt(enchantment.split("\\s")[1]), true);
            }
            item.setItemMeta(EnchantMeta);
        }

        return item;
    }
}