package me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.MaterialComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MMOItemsComponent implements MaterialComponent {
    @Override
    public boolean isCorrectTag(String tag) {
        return tag.toLowerCase().startsWith("[mmo]");
    }

    @Override
    public ItemStack createItem(Context ctx, String value, Player player, PanelItem item) {
        String itemType = value.split("\\s")[0];
        String itemID = value.split("\\s")[1];

        MMOItem mmoitem = MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(itemType), itemID);
        if(mmoitem != null){
            return mmoitem.newBuilder().build();
        }
        return null;
    }
}