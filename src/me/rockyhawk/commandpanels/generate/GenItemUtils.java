package me.rockyhawk.commandpanels.generate;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.rockyhawk.commandpanels.Context;
import org.bukkit.Bukkit;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GenItemUtils {

    Context ctx;

    public GenItemUtils(Context pl) {
        this.ctx = pl;
    }

    @SuppressWarnings("deprecation")
    public YamlConfiguration generatePanelFile(String panelName, Inventory inv, YamlConfiguration file){
        ItemStack cont;
        for(int i = 0; inv.getSize() > i; i++){
            cont = inv.getItem(i);
            //repeat through all the items in the editor
            try{
                //make the item here
                if(cont == null){
                    //remove if items have been removed
                    if(file.contains("panels." + panelName + ".item." + i)){
                        //if the material doesn't equal air (don't delete air materials in the editor)
                        if(!file.getString("panels." + panelName + ".item." + i + ".material").equalsIgnoreCase("AIR")) {
                            file.set("panels." + panelName + ".item." + i, null);
                            continue;
                        }
                    }
                }
                if(ctx.version.isBelow("1.13")){
                    if (cont.getDurability() != 0 && !cont.getType().toString().equals("SKULL_ITEM")) {
                        file.set("panels." + panelName + ".item." + i + ".ID", cont.getDurability());
                    }
                }
                if(file.contains("panels." + panelName + ".item." + i + ".material")){
                    if(Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("%") || Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("=")){
                        if(!cont.getType().toString().equalsIgnoreCase("PLAYER_HEAD") || cont.getType().toString().equalsIgnoreCase("SKULL_ITEM")){
                            file.set("panels." + panelName + ".item." + i + ".material", cont.getType().toString());
                        }
                    }else{
                        file.set("panels." + panelName + ".item." + i + ".material", cont.getType().toString());
                    }
                }else{
                    file.set("panels." + panelName + ".item." + i + ".material", cont.getType().toString());
                }
                if(cont.getType().toString().equalsIgnoreCase("PLAYER_HEAD") || cont.getType().toString().equalsIgnoreCase("SKULL_ITEM")){
                    if(!Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("%") && !Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("=")) {
                        file.set("panels." + panelName + ".item." + i + ".material",
                                ctx.version.isBelow("1.13") ? "SKULL_ITEM" : "PLAYER_HEAD");
                    }
                }
                try {
                    BannerMeta bannerMeta = (BannerMeta) cont.getItemMeta();
                    List<String> dyePattern = new ArrayList<>();
                    for(Pattern pattern : bannerMeta.getPatterns()) { //sublist to skip first value
                        dyePattern.add(pattern.getColor() + "," + pattern.getPattern());
                    }
                    file.set("panels." + panelName + ".item." + i + ".banner", dyePattern);
                }catch(Exception ignore){
                    //not a banner
                    file.set("panels." + panelName + ".item." + i + ".banner", null);
                }
                try {
                    //potion legacy PotionData or current PotionType
                    if(ctx.version.isBelow("1.20.5")){
                        if(ctx.potion_1_20_4.retrievePotionData(cont) != null) {
                            file.set("panels." + panelName + ".item." + i + ".potion", ctx.potion_1_20_4.retrievePotionData(cont));
                        }
                    }else{
                        PotionMeta potionMeta = (PotionMeta) cont.getItemMeta();
                        assert potionMeta != null;
                        String potionType = potionMeta.getBasePotionType().toString(); // Gets the potion type as a string rather than bukkit type
                        file.set("panels." + panelName + ".item." + i + ".potion", potionType);
                    }
                }catch(Exception ignore){
                    //not a banner
                    file.set("panels." + panelName + ".item." + i + ".potion", null);
                }
                file.set("panels." + panelName + ".item." + i + ".stack", cont.getAmount());
                if(!cont.getEnchantments().isEmpty()){
                    Map<Enchantment, Integer> enchantments = cont.getEnchantments();
                    List<String> newEnchantments = new ArrayList<>();
                    for(Enchantment enchantment : enchantments.keySet()){
                        newEnchantments.add(enchantment.getKey().getKey() + " " + enchantments.get(enchantment));
                    }
                    file.set("panels." + panelName + ".item." + i + ".enchanted", newEnchantments);
                }
                file.set("panels." + panelName + ".item." + i + ".name", Objects.requireNonNull(cont.getItemMeta()).getDisplayName());
                file.set("panels." + panelName + ".item." + i + ".lore", Objects.requireNonNull(cont.getItemMeta()).getLore());
                if(ctx.version.isAtLeast("1.14")){
                    if(cont.getItemMeta().hasCustomModelData()){
                        file.set("panels." + panelName + ".item." + i + ".customdata", Objects.requireNonNull(cont.getItemMeta()).getCustomModelData());
                    }
                }
                if(ctx.version.isAtLeast("1.21.4")){
                    try {
                        // Check if the getItemModel method exists
                        Method getItemModelMethod = ItemMeta.class.getMethod("getItemModel");

                        // Invoke it dynamically
                        Object itemModelData = getItemModelMethod.invoke(cont.getItemMeta());

                        file.set("panels." + panelName + ".item." + i + ".itemmodel", Objects.requireNonNull(itemModelData));
                    } catch (NoSuchMethodException e) {
                        // The method does not exist in older Spigot versions
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Try getting item NBT
                try {
                    NBTItem nbtItem = new NBTItem(cont); // Convert item to NBTItem

                    // Base path where NBT data is stored in the YAML file
                    String basePath = "panels." + panelName + ".item." + i + ".nbt";

                    // Iterate over all NBT keys
                    for (String key : nbtItem.getKeys()) {
                        Object value = ctx.nbt.getNBTValue(nbtItem.getItem(), key); // Retrieve dynamically

                        if (value == null) continue; // Skip null values

                        if (value instanceof Map) {
                            // Save nested NBT compounds properly
                            ConfigurationSection subSection = file.createSection(basePath + "." + key);
                            ctx.nbt.saveMapToYAML((Map<String, Object>) value, subSection);
                        } else {
                            // Directly set primitive values
                            file.set(basePath + "." + key, value);
                        }
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error while saving NBT data: " + e.getMessage());
                    e.printStackTrace();
                }
            }catch(Exception n){
                //skip over an item that spits an error - Usually air
            }
        }
        return file;
    }
}
