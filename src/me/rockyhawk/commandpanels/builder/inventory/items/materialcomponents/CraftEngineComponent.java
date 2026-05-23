package me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.MaterialComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CraftEngineComponent implements MaterialComponent {
    @Override
    public boolean isCorrectTag(String tag) {
        return tag.toLowerCase().startsWith("[craftengine]");
    }

    @Override
    public ItemStack createItem(Context ctx, String itemID, Player player, PanelItem item) {
        var definition = CraftEngineItems.byId(itemID);

        if (definition == null) {
            return null;
        }

        return definition.buildBukkitItem(player);
    }
}