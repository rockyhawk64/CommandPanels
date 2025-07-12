package me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.MaterialComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NexoComponent implements MaterialComponent {
    @Override
    public boolean isCorrectTag(String tag) {
        return tag.toLowerCase().startsWith("[nexo]");
    }

    @Override
    public ItemStack createItem(Context ctx, String itemID, Player player, PanelItem item) {
        try {
            ItemBuilder builder = NexoItems.itemFromId(itemID);
            if (builder != null) {
                return builder.build();
            }
        } catch (Exception e) {
            ctx.text.sendError(player, "Error with Nexo Material Tag: " + itemID);
        }
        return null;
    }
}