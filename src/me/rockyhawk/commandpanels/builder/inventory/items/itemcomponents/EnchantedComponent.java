package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.Subst;

import java.util.HashMap;
import java.util.Map;

public class EnchantedComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.enchantments().isEmpty()) return itemStack;

        Map<Enchantment,Integer> enchantments = new HashMap<>();

        for(String enchantment : item.enchantments()){
            String[] value = ctx.text.parseTextToString(player, enchantment)
                    .toLowerCase()
                    .split("\\s");

            @Subst("") String enchant = value[0].contains(":") ? value[0] : "minecraft:" + value[0];
            int level = Integer.parseInt(value[1]);

            Enchantment enchantmentVal = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.ENCHANTMENT)
                    .get(Key.key(enchant));

            enchantments.put(enchantmentVal, level);
        }

        itemStack.setData(
                DataComponentTypes.ENCHANTMENTS,
                ItemEnchantments.itemEnchantments(enchantments)
        );

        return itemStack;
    }
}