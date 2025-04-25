package me.rockyhawk.commandpanels.items.builder.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.ItemComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomDataComponent implements ItemComponent {

    @Override
    public ItemStack apply(ItemStack item, ConfigurationSection section, Context ctx, Player player, Panel panel, PanelPosition pos, boolean addNBT) {
        if(!section.contains("customdata")) return item;

        ItemMeta customMeta = item.getItemMeta();
        assert customMeta != null;
        customMeta.setCustomModelData(Integer.parseInt(ctx.text.placeholders(panel,pos,player,section.getString("customdata"))));
        item.setItemMeta(customMeta);
        
        return item;
    }
}