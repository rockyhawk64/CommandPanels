package me.rockyhawk.commandpanels.items.builder.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.ItemComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.Objects;

public class PotionColorComponent implements ItemComponent {

    @Override
    public ItemStack apply(ItemStack item, ConfigurationSection section, Context ctx, Player player, Panel panel, PanelPosition pos, boolean addNBT) {
        if(!section.contains("potion-color")) return item;

        if(ctx.version.isBelow("1.11")) {
            player.sendMessage(ChatColor.RED + "Potion Colors are 1.11+");
            return item;
        }

        String[] rgb = Objects.requireNonNull(section.getString("potion-color")).split(",");
        Color color = Color.fromRGB(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
        PotionMeta potionMeta = (PotionMeta)item.getItemMeta();
        assert potionMeta != null;
        potionMeta.setColor(color);
        item.setItemMeta(potionMeta);
        return item;
    }
}