package me.rockyhawk.commandpanels.ioclasses.nbt;

import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface NBTHandler {
    boolean hasSameNBT(ItemStack one, ItemStack two);
    boolean hasNBT(ItemStack item, String key);
    ItemStack setNBT(ItemStack item, String key, Object value);
    Object getNBTValue(ItemStack item, String key);
    void applyNBTRecursively(ItemStack item, ConfigurationSection section, Player player, Panel panel, PanelPosition position);
    void saveMapToYAML(Map<String, Object> map, ConfigurationSection section);
}