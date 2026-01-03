package me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.MaterialComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CraftEngineComponent implements MaterialComponent {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.toLowerCase().startsWith("[craftengine]");
    }

    @Override
    public ItemStack createItem(Context ctx, String itemID, Player player, PanelItem item) {
        CustomItem<ItemStack> customItem = CraftEngineItems.byId(Key.of(itemID));
        if (customItem != null) {
            return customItem.buildItemStack(BukkitAdaptors.adapt(player));
        }
        return null;
    }

}
