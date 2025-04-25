package me.rockyhawk.commandpanels.items.builder.materialcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.MaterialComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.manager.ItemManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MMOItemsComponent implements MaterialComponent {
    @Override
    public boolean matches(String tag) {
        return tag.toLowerCase().startsWith("mmo=");
    }

    @Override
    public ItemStack createItem(String tag, Player player, Context ctx, ConfigurationSection section, Panel panel, PanelPosition pos) {
        String itemType = tag.split("\\s")[1];
        String itemID = tag.split("\\s")[2];
        ItemManager itemManager = MMOItems.plugin.getItems();
        MMOItem mmoitem = itemManager.getMMOItem(MMOItems.plugin.getTypes().get(itemType), itemID);
        return mmoitem.newBuilder().build();
    }
}
