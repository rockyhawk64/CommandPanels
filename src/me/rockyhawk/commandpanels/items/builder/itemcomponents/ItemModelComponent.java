package me.rockyhawk.commandpanels.items.builder.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.ItemComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;

public class ItemModelComponent implements ItemComponent {

    @Override
    public ItemStack apply(ItemStack item, ConfigurationSection section, Context ctx, Player player, Panel panel, PanelPosition pos, boolean addNBT) {
        if(!section.contains("itemmodel")) return item;

        //Item Model 1.21.4+
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;

        try {
            // Check if the setHideTooltip method exists
            Method setItemModelMethod = ItemMeta.class.getMethod("setItemModel", NamespacedKey.class);

            // Invoke it dynamically
            setItemModelMethod.invoke(itemMeta, NamespacedKey.fromString(ctx.text.placeholders(panel, pos, player, section.getString("itemmodel"))));

            item.setItemMeta(itemMeta);
        } catch (NoSuchMethodException e) {
            // The method does not exist in older Spigot versions
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }
}