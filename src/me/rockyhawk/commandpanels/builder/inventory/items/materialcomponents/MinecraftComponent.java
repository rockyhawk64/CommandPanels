package me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.MaterialComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.Bukkit;
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
        String materialId = itemID;
        String components = null;

        int bracketIndex = itemID.indexOf('[');
        if (bracketIndex != -1) {
            materialId = itemID.substring(0, bracketIndex);
            components = itemID.substring(bracketIndex); // includes components in the material
        }

        Material material = Material.matchMaterial(materialId.toUpperCase());
        if (material == null) return null;

        ItemStack stack = new ItemStack(material);

        // If the material contains components, add them to the item
        if (components != null) {
            // Returns original stack if malformed
            stack = Bukkit.getUnsafe().modifyItemStack(stack, components);
        }

        return stack;
    }
}
