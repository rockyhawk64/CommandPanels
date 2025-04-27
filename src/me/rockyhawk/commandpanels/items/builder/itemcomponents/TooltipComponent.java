package me.rockyhawk.commandpanels.items.builder.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.ItemComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;

public class TooltipComponent implements ItemComponent {

    @Override
    public ItemStack apply(ItemStack item, ConfigurationSection section, Context ctx, Player player, Panel panel, PanelPosition pos, boolean addNBT) {
        if(!section.contains("tooltip")) return item;

        //Tooltip Style 1.21.4+
        if(ctx.version.isBelow("1.21.4")) {
            player.sendMessage(ChatColor.RED + "Custom tooltips are 1.21.4+");
            return item;
        }

        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;

        try {
            // Check if the setHideTooltip method exists
            Method setTooltipMethod = ItemMeta.class.getMethod("setTooltipStyle", NamespacedKey.class);

            // Invoke it dynamically
            setTooltipMethod.invoke(itemMeta, NamespacedKey.fromString(ctx.text.placeholders(panel, pos, player, section.getString("tooltip"))));

            item.setItemMeta(itemMeta);
        } catch (NoSuchMethodException e) {
            // The method does not exist in older Spigot versions
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }
}