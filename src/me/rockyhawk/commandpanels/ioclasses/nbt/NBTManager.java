package me.rockyhawk.commandpanels.ioclasses.nbt;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NBTManager {
    CommandPanels plugin;
    public NBTManager(CommandPanels pl) {
        this.plugin = pl;
    }

    public boolean hasSameNBT(ItemStack one, ItemStack two){
        NBTItem nbtitem1 = new NBTItem(one);
        NBTItem nbtitem2 = new NBTItem(two);

        return nbtitem1.equals(nbtitem2);
    }

    public ItemStack setNBT(ItemStack item, String key, String type, String value) {
        type = type.toLowerCase();
        if (item == null) {
            throw new IllegalArgumentException("ItemStack cannot be null");
        }
        NBTItem nbtItem = new NBTItem(item);

        switch (type.toLowerCase()) {
            case "byte":
                nbtItem.setByte(key, Byte.valueOf(value));
                break;
            case "boolean":
                nbtItem.setBoolean(key, Boolean.valueOf(value));
                break;
            case "short":
                nbtItem.setShort(key, Short.valueOf(value));
                break;
            case "integer":
                nbtItem.setInteger(key, Integer.valueOf(value));
                break;
            case "long":
                nbtItem.setLong(key, Long.valueOf(value));
                break;
            case "float":
                nbtItem.setFloat(key, Float.valueOf(value));
                break;
            case "double":
                nbtItem.setDouble(key, Double.valueOf(value));
                break;
            case "string":
                nbtItem.setString(key, value);
                break;
            default:
                throw new IllegalArgumentException("Unsupported NBT type: " + type);
        }
        item.setItemMeta(nbtItem.getItem().getItemMeta());
        return item;
    }

    public Object getNBT(ItemStack item, String key, String type) {
        type = type.toLowerCase();
        NBTItem nbtItem = new NBTItem(item);
        switch(type.toLowerCase()) {
            case "byte":
                return nbtItem.getByte(key);
            case "boolean":
                return nbtItem.getBoolean(key);
            case "short":
                return nbtItem.getShort(key);
            case "integer":
                return nbtItem.getInteger(key);
            case "long":
                return nbtItem.getLong(key);
            case "float":
                return nbtItem.getFloat(key);
            case "double":
                return nbtItem.getDouble(key);
            case "string":
                return nbtItem.getString(key);
            default:
                throw new IllegalArgumentException("Unsupported NBT type: " + type);
        }
    }

    public boolean hasNBT(ItemStack item, String key){
        NBTItem nbti = new NBTItem(item);
        return nbti.hasTag(key);
    }

    //nbt will be assigned with the value "string_value_1
    //which uses the type "String" and value "value_1"
    public void applyNBTRecursively(String path, ConfigurationSection section, ItemStack s, Player p, Panel panel, PanelPosition position) {
        for (String key : section.getKeys(true)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            if (section.isConfigurationSection(key)) {
                // Recursive call for nested sections
                applyNBTRecursively(fullPath, section.getConfigurationSection(key), s, p, panel, position);
            } else {
                // Set the NBT tag at this level
                String wholeValue = plugin.tex.attachPlaceholders(panel, position, p, section.getString(key));
                int firstUnderscore = wholeValue.indexOf("_");
                String type = wholeValue.substring(0, firstUnderscore);
                String value = wholeValue.substring(firstUnderscore + 1);
                s = plugin.nbt.setNBT(s, fullPath, type, value);
            }
        }
    }
}