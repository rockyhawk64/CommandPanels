package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemArmorTrim;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class TrimComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if (item.armorTrim() == null) return itemStack;

        // trim: <Material> <Pattern>
        String[] trimList = ctx.text.parseTextToString(player, item.armorTrim()).split("\\s+");
        if (trimList.length != 2) return itemStack;

        String trimMaterialString = trimList[0].toLowerCase();
        String trimPatternString = trimList[1].toLowerCase();

        // Use registry lookups directly
        TrimMaterial trimMaterial = Registry.TRIM_MATERIAL.get(NamespacedKey.fromString("minecraft:" + trimMaterialString));
        TrimPattern trimPattern = Registry.TRIM_PATTERN.get(NamespacedKey.fromString("minecraft:" + trimPatternString));

        // Only apply if both exist
        if (trimMaterial != null && trimPattern != null) {
            ArmorTrim trim = new ArmorTrim(trimMaterial, trimPattern);
            itemStack.setData(DataComponentTypes.TRIM, ItemArmorTrim.itemArmorTrim(trim));
        }

        return itemStack;
    }
}
