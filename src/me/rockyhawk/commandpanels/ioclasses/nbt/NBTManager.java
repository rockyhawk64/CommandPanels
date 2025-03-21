package me.rockyhawk.commandpanels.ioclasses.nbt;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTType;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class NBTManager {
    CommandPanels plugin;

    public NBTManager(CommandPanels pl) {
        this.plugin = pl;
    }

    public boolean hasSameNBT(ItemStack one, ItemStack two) {
        return new NBTItem(one).equals(new NBTItem(two));
    }

    public boolean hasNBT(ItemStack item, String key) {
        return new NBTItem(item).hasTag(key);
    }

    public ItemStack setNBT(ItemStack item, String key, Object value) {
        if (item == null || item.getType() == Material.AIR) return item;

        NBTItem nbtItem = new NBTItem(item);
        setNBTValue(nbtItem, key, value);
        item.setItemMeta(nbtItem.getItem().getItemMeta());

        return item;
    }

    public void applyNBTRecursively(ItemStack item, ConfigurationSection section, Player player, Panel panel, PanelPosition position) {
        if (item == null || item.getType() == Material.AIR) return;

        NBTItem nbtItem = new NBTItem(item);

        for (String key : section.getKeys(false)) {
            Object value = section.get(key);

            if (section.isConfigurationSection(key)) {
                NBTCompound compound = nbtItem.addCompound(key);
                convertSectionToNBT(compound, section.getConfigurationSection(key), player, panel, position);
            } else {
                setNBTValue(nbtItem, key, value);
            }
        }

        item.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    public void saveMapToYAML(Map<String, Object> map, ConfigurationSection section) {
        if (map == null || section == null) return;

        map.forEach((key, value) -> {
            if (value instanceof Map) {
                saveMapToYAML((Map<String, Object>) value, section.createSection(key));
            } else if (value instanceof Byte) {
                // Convert only Byte values that are actually Boolean representations
                byte byteValue = (Byte) value;
                if (byteValue == 1 || byteValue == 0) {
                    section.set(key, byteValue == 1);
                } else {
                    section.set(key, byteValue);
                }
            } else {
                section.set(key, value);
            }
        });
    }

    private void convertSectionToNBT(NBTCompound compound, ConfigurationSection section, Player player, Panel panel, PanelPosition position) {
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);

            if (section.isConfigurationSection(key)) {
                // Properly handles nested compounds instead of treating them as strings
                convertSectionToNBT(compound.addCompound(key), section.getConfigurationSection(key), player, panel, position);
            } else {
                setNBTValue(compound, key, value);
            }
        }
    }

    private void setNBTValue(NBTCompound compound, String key, Object value) {
        if (value instanceof Boolean) {
            compound.setByte(key, (Boolean) value ? (byte) 1 : (byte) 0); // Store as NBT Byte since boolean is shown as 1b or 0b
        } else if (value instanceof Integer) {
            compound.setInteger(key, (Integer) value);
        } else if (value instanceof Double) {
            compound.setDouble(key, (Double) value);
        } else if (value instanceof Long) {
            compound.setLong(key, (Long) value);
        } else if (value instanceof Short) {
            compound.setShort(key, (Short) value);
        } else if (value instanceof Float) {
            compound.setFloat(key, (Float) value);
        } else if (value instanceof Byte) {
            compound.setByte(key, (Byte) value);
        } else if (value instanceof Map) {
            saveMapToNBTCompound((Map<String, Object>) value, compound.addCompound(key));
        } else {
            compound.setString(key, value.toString());
        }
    }

    public Object getNBTValue(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR) return null;

        NBTItem nbtItem = new NBTItem(item);
        if (!nbtItem.hasTag(key)) return null;

        return extractNBTValue(nbtItem, key, nbtItem.getType(key));
    }

    private Object extractNBTValue(NBTCompound compound, String key, NBTType type) {
        switch (type) {
            case NBTTagInt:
                return compound.getInteger(key);
            case NBTTagDouble:
                return compound.getDouble(key);
            case NBTTagByte:
                byte byteValue = compound.getByte(key);
                if (byteValue == 1 || byteValue == 0) {
                    return byteValue == 1; // Convert to Boolean since its displayed as 1b and 0b
                }
                return byteValue;
            case NBTTagFloat:
                return compound.getFloat(key);
            case NBTTagLong:
                return compound.getLong(key);
            case NBTTagShort:
                return compound.getShort(key);
            case NBTTagByteArray:
                return compound.getByteArray(key);
            case NBTTagIntArray:
                return compound.getIntArray(key);
            case NBTTagList:
                return compound.getStringList(key); // Assuming it's a String list
            case NBTTagCompound:
                return convertCompoundToMap(compound.getCompound(key)); // Recursively convert sub-compounds
            case NBTTagString:
                String str = compound.getString(key);
                if (str.equalsIgnoreCase("true")) return true;
                if (str.equalsIgnoreCase("false")) return false;
                if (str.matches("-?\\d+")) return Integer.parseInt(str);
                if (str.matches("-?\\d+\\.\\d+")) return Double.parseDouble(str);
                return str;
            default:
                return null;
        }
    }


    private Map<String, Object> convertCompoundToMap(NBTCompound compound) {
        Map<String, Object> compoundMap = new LinkedHashMap<>();
        compound.getKeys().forEach(key -> compoundMap.put(key, extractNBTValue(compound, key, compound.getType(key))));
        return compoundMap;
    }

    private void saveMapToNBTCompound(Map<String, Object> map, NBTCompound compound) {
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                saveMapToNBTCompound((Map<String, Object>) value, compound.addCompound(key));
            } else {
                setNBTValue(compound, key, value);
            }
        });
    }
}