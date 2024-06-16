package me.rockyhawk.commandpanels.ioclasses.nbt;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Material;
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
    // Method to apply NBT recursively
    public ItemStack setNBT(ItemStack item, String key, String value) {
        if (item == null || item.getType() == Material.AIR) {
            return item;
        }
        NBTItem nbtItem = new NBTItem(item);
        setNBTDirectlyOnItem(nbtItem, key, value);

        item.setItemMeta(nbtItem.getItem().getItemMeta());
        return item;
    }

    // Method to apply NBT recursively
    public void applyNBTRecursively(ItemStack item, ConfigurationSection section, Player player, Panel panel, PanelPosition position) {
        NBTItem nbtItem = new NBTItem(item);

        // Iterate over each key in the root of the ConfigurationSection
        for (String key : section.getKeys(false)) {
            // Check if the key represents a ConfigurationSection
            if (section.isConfigurationSection(key)) {
                // Create a compound for each ConfigurationSection
                NBTCompound compound = nbtItem.addCompound(key);
                convertSectionToNBT(compound, section.getConfigurationSection(key), player, panel, position);
            } else {
                // Handle non-section values directly on the NBTItem if necessary
                String value = plugin.tex.attachPlaceholders(panel, position, player, section.getString(key));
                setNBTDirectlyOnItem(nbtItem, key, value);
            }
        }
        item.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    // Convert ConfigurationSection to NBTCompound recursively
    private void convertSectionToNBT(NBTCompound compound, ConfigurationSection section, Player player, Panel panel, PanelPosition position) {
        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                // Recursively convert sub-sections to their own compounds
                NBTCompound subCompound = compound.addCompound(key);
                convertSectionToNBT(subCompound, section.getConfigurationSection(key), player, panel, position);
            } else {
                // Handle scalar values within each compound
                String value = plugin.tex.attachPlaceholders(panel, position, player, section.getString(key));
                setNBTOnCompound(compound, key, value);
            }
        }
    }

    private void setNBTDirectlyOnItem(NBTItem nbtItem, String key, String value) {
        int underscoreIndex = value.indexOf("_");
        if (underscoreIndex == -1) return; // Skip if format is invalid

        String type = value.substring(0, underscoreIndex);
        String val = value.substring(underscoreIndex + 1);

        switch (type.toLowerCase()) {
            case "byte":
                nbtItem.setByte(key, Byte.parseByte(val));
                break;
            case "boolean":
                nbtItem.setBoolean(key, Boolean.parseBoolean(val));
                break;
            case "short":
                nbtItem.setShort(key, Short.parseShort(val));
                break;
            case "integer":
                nbtItem.setInteger(key, Integer.parseInt(val));
                break;
            case "long":
                nbtItem.setLong(key, Long.parseLong(val));
                break;
            case "float":
                nbtItem.setFloat(key, Float.parseFloat(val));
                break;
            case "double":
                nbtItem.setDouble(key, Double.parseDouble(val));
                break;
            case "string":
                nbtItem.setString(key, val);
                break;
            default:
                throw new IllegalArgumentException("Unsupported NBT type: " + type);
        }
    }

    // Set typed value on an NBTCompound
    private void setNBTOnCompound(NBTCompound compound, String key, String value) {
        int underscoreIndex = value.indexOf("_");
        if (underscoreIndex == -1) return; // Invalid format, skip

        String type = value.substring(0, underscoreIndex);
        String val = value.substring(underscoreIndex + 1);

        switch (type.toLowerCase()) {
            case "byte":
                compound.setByte(key, Byte.parseByte(val));
                break;
            case "boolean":
                compound.setBoolean(key, Boolean.parseBoolean(val));
                break;
            case "short":
                compound.setShort(key, Short.parseShort(val));
                break;
            case "integer":
                compound.setInteger(key, Integer.parseInt(val));
                break;
            case "long":
                compound.setLong(key, Long.parseLong(val));
                break;
            case "float":
                compound.setFloat(key, Float.parseFloat(val));
                break;
            case "double":
                compound.setDouble(key, Double.parseDouble(val));
                break;
            case "string":
                compound.setString(key, val);
                break;
            default:
                throw new IllegalArgumentException("Unsupported NBT type: " + type);
        }
    }
}