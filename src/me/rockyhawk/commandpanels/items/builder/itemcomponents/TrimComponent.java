package me.rockyhawk.commandpanels.items.builder.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.ItemComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TrimComponent implements ItemComponent {

    @Override
    public ItemStack apply(ItemStack item, ConfigurationSection section, Context ctx, Player player, Panel panel, PanelPosition pos, boolean addNBT) {
        if(!section.contains("trim")) return item;

        if(ctx.version.isAtLeast("1.20")){
            // trim: <Material> <Pattern>
            String trim = section.getString("trim");
            String[] trimList = trim.split("\\s");
            if(trimList.length == 2){
                String trimMaterialString = trimList[0].toLowerCase();
                String trimPatternString = trimList[1].toLowerCase();

                // Check if Material and Pattern are valid and the itemstack is an armor piece
                if(isTrimMaterial(trimMaterialString) && isTrimPattern(trimPatternString) && isArmor(item)){

                    // Getting the correct Pattern and Material - Seems to be experimental this way
                    // Material and Pattern don't have a valueOf-function to get them the easier way.
                    TrimMaterial trimMaterial = Registry.TRIM_MATERIAL.get(Objects.requireNonNull(NamespacedKey.fromString("minecraft:" + trimMaterialString)));
                    TrimPattern trimPattern = Registry.TRIM_PATTERN.get(Objects.requireNonNull(NamespacedKey.fromString("minecraft:" + trimPatternString)));

                    ArmorMeta armorMeta = (ArmorMeta) item.getItemMeta();
                    armorMeta.setTrim(new ArmorTrim(trimMaterial, trimPattern));
                    item.setItemMeta(armorMeta);
                }
            }
        }

        return item;
    }

    private boolean isTrimMaterial(String material){
        List<String> availableMaterial = Arrays.asList("AMETHYST",
                "COPPER", "DIAMOND", "EMERALD", "GOLD", "IRON","LAPIS", "NETHERITE", "QUARTZ", "REDSTONE");
        return availableMaterial.contains(material.toUpperCase());
    }

    private boolean isTrimPattern(String pattern){
        List<String> availablePattern = Arrays.asList("COAST",
                "DUNE", "EYE", "HOST", "RAISER", "RIB","SENTRY", "SHAPER", "SILENCE", "SNOUT", "SPIRE", "TIDE","VEX", "WARD", "WAYFINDER", "WILD");
        return availablePattern.contains(pattern.toUpperCase());
    }

    private boolean isArmor(ItemStack stack){
        return EnchantmentTarget.ARMOR.includes(stack);
    }
}