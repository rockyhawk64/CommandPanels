package me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.MaterialComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MinecraftComponent implements MaterialComponent {
    @Override
    public boolean isCorrectTag(String tag) {
        return tag.toLowerCase().startsWith("[mc]");
    }

    @Override
    public ItemStack createItem(Context ctx, String itemID, Player player, PanelItem item) {
        Material mat = Material.matchMaterial(itemID.toUpperCase());
        if (mat == null) return null;
        return new ItemStack(mat);
    }
}
