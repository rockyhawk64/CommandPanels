package me.rockyhawk.commandpanels.ioclasses.nbt;

import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class PersistentDataHandler implements NBTHandler {
    private final Plugin plugin;

    public PersistentDataHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean hasSameNBT(ItemStack one, ItemStack two) {
        return getAllKeys(one).equals(getAllKeys(two)); // basic compare
    }

    public boolean hasNBT(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(new NamespacedKey(plugin, key), PersistentDataType.STRING);
    }

    public ItemStack setNBT(ItemStack item, String key, Object value) {
        if (item == null || item.getType() == Material.AIR) return item;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, key), PersistentDataType.STRING, value.toString()
        );
        item.setItemMeta(meta);
        return item;
    }

    public Object getNBTValue(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR) return null;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.get(new NamespacedKey(plugin, key), PersistentDataType.STRING);
    }

    private String getAllKeys(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return "";
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.getKeys().toString(); // basic comparison
    }

    public void applyNBTRecursively(ItemStack item, ConfigurationSection section, Player player, Panel panel, PanelPosition position) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        applySectionToContainer(container, section);
        item.setItemMeta(meta);
    }

    private void applySectionToContainer(PersistentDataContainer container, ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (section.isConfigurationSection(key)) {
                NamespacedKey nsKey = new NamespacedKey("myplugin", key);
                PersistentDataContainer subContainer = container.getAdapterContext().newPersistentDataContainer();
                applySectionToContainer(subContainer, section.getConfigurationSection(key));
                container.set(nsKey, PersistentDataType.TAG_CONTAINER, subContainer);
            } else {
                setValueInContainer(container, key, value);
            }
        }
    }

    private void setValueInContainer(PersistentDataContainer container, String key, Object value) {
        NamespacedKey nsKey = new NamespacedKey("myplugin", key);
        if (value instanceof Boolean) {
            container.set(nsKey, PersistentDataType.BYTE, (byte) ((Boolean) value ? 1 : 0));
        } else if (value instanceof Integer) {
            container.set(nsKey, PersistentDataType.INTEGER, (Integer) value);
        } else if (value instanceof Double) {
            container.set(nsKey, PersistentDataType.DOUBLE, (Double) value);
        } else if (value instanceof Long) {
            container.set(nsKey, PersistentDataType.LONG, (Long) value);
        } else if (value instanceof Float) {
            container.set(nsKey, PersistentDataType.FLOAT, (Float) value);
        } else if (value instanceof Byte) {
            container.set(nsKey, PersistentDataType.BYTE, (Byte) value);
        } else {
            container.set(nsKey, PersistentDataType.STRING, value.toString());
        }
    }

    public void saveMapToYAML(Map<String, Object> map, ConfigurationSection section) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                ConfigurationSection newSection = section.createSection(entry.getKey());
                saveMapToYAML((Map<String, Object>) value, newSection);
            } else if (value instanceof Byte) {
                byte byteValue = (Byte) value;
                if (byteValue == 1 || byteValue == 0) {
                    section.set(entry.getKey(), byteValue == 1);
                } else {
                    section.set(entry.getKey(), byteValue);
                }
            } else {
                section.set(entry.getKey(), value);
            }
        }
    }

}
