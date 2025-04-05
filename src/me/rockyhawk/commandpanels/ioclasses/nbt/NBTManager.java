package me.rockyhawk.commandpanels.ioclasses.nbt;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/*
NBT will use NBTAPI, and if that doesn't work in the MC version necessary will instead use PersistentData

NBTManager.java            <-- Main entry point, handles logic and fallback
NBTHandler.java            <-- Interface
NBTAPIHandler.java         <-- Primary handler using NBTAPI for NBT
PersistentDataHandler.java <-- Fallback using PersistentDataContainer
 */

public class NBTManager {
    private final NBTHandler handler;

    public NBTManager(CommandPanels plugin) {
        NBTHandler nbtHandler;

        try {
            // Try using NBTAPIHandler
            nbtHandler = new NBTAPIHandler();
            ItemStack test = new ItemStack(Material.STONE);
            String testKey = "nbt_test_key";
            String testValue = "hello";

            ItemStack modified = nbtHandler.setNBT(test, testKey, testValue);
            Object result = nbtHandler.getNBTValue(modified, testKey);

            //Should never happen, would indicate an issue with NBTAPI
            if (result == null || !result.equals(testValue)) {
                throw new IllegalStateException("NBTAPI test failed – values didn't match.");
            }
        } catch (Throwable ex) {
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[CommandPanels] NBTAPI Error: version being used may not be compatible with this version of Minecraft. Falling back to use PersistentData.");
            nbtHandler = new PersistentDataHandler(plugin);
        }

        this.handler = nbtHandler;
    }

    public boolean hasSameNBT(ItemStack one, ItemStack two) {
        return handler.hasSameNBT(one, two);
    }

    public boolean hasNBT(ItemStack item, String key) {
        return handler.hasNBT(item, key);
    }

    public ItemStack setNBT(ItemStack item, String key, Object value) {
        return handler.setNBT(item, key, value);
    }

    public Object getNBTValue(ItemStack item, String key) {
        return handler.getNBTValue(item, key);
    }

    public void applyNBTRecursively(ItemStack item, ConfigurationSection section, Player player, Panel panel, PanelPosition position) {
        handler.applyNBTRecursively(item, section, player, panel, position);
    }

    public void saveMapToYAML(Map<String, Object> map, ConfigurationSection section) {
        handler.saveMapToYAML(map, section);
    }
}