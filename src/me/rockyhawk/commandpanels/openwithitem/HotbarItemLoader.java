package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public class HotbarItemLoader {
    private final CommandPanels plugin;
    private final HashSet<UUID> disabledHotbars = new HashSet<>();
    private final File configFile;
    private final FileConfiguration config;

    // Stationary slots map for managing items per player
    private final HashMap<UUID, HotbarPlayerManager> stationaryItems = new HashMap<>();

    public HotbarItemLoader(CommandPanels plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "hotbar_preferences.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
        loadPreferences();
    }

    // Toggle hotbar items for a player
    public boolean toggleHotbarItems(Player player) {
        UUID playerId = player.getUniqueId();
        if (disabledHotbars.contains(playerId)) {
            disabledHotbars.remove(playerId);
            savePreferences();
            updateHotbarItems(player); // Re-enable items
            return true;
        } else {
            disabledHotbars.add(playerId);
            savePreferences();
            removeHotbarItems(player); // Remove items
            return false;
        }
    }

    // Check if hotbar items are disabled for a player
    private boolean isHotbarDisabled(Player player) {
        return disabledHotbars.contains(player.getUniqueId());
    }

    // Remove all hotbar items for a player
    private void removeHotbarItems(Player player) {
        for (int i = 0; i <= 35; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isValidHotbarItem(item, "CommandPanelsHotbar")) {
                player.getInventory().setItem(i, new ItemStack(Material.AIR));
            }
        }
        player.updateInventory();
    }

    // Validate if an item is a valid hotbar item
    private boolean isValidHotbarItem(ItemStack item, String nbtKey) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        try {
            String nbtData = String.valueOf(plugin.nbt.getNBT(item, nbtKey, "string"));
            return nbtData != null && !nbtData.isEmpty();
        } catch (Exception ex) {
            return false;
        }
    }

    // Update hotbar items, but skip if disabled
    public void updateHotbarItems(Player player) {
        if (isHotbarDisabled(player)) {
            return; // Skip updating if disabled
        }

        // Stationary Items initialisieren
        stationaryItems.put(player.getUniqueId(), new HotbarPlayerManager());

        // Alte Items entfernen
        for (int i = 0; i <= 35; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isValidHotbarItem(item, "CommandPanelsHotbar")) {
                try {
                    String nbtData = String.valueOf(plugin.nbt.getNBT(item, "CommandPanelsHotbar", "string"));
                    if (!nbtData.endsWith("-1")) {
                        player.getInventory().setItem(i, new ItemStack(Material.AIR));
                    }
                } catch (Exception ignore) {}
            }
        }

        // Neue Items hinzufügen
        for (Panel panel : plugin.panelList) {
            if (!plugin.panelPerms.isPanelWorldEnabled(player, panel.getConfig())) {
                continue;
            }
            if (player.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm")) && panel.hasHotbarItem()) {
                ItemStack hotbarItem = panel.getHotbarItem(player);
                if (panel.getHotbarSection(player).contains("stationary")) {
                    int slot = Integer.parseInt(panel.getHotbarSection(player).getString("stationary"));
                    player.getInventory().setItem(slot, hotbarItem);
                    stationaryItems.get(player.getUniqueId()).addSlot(String.valueOf(slot), panel);
                }
            }
        }

        player.updateInventory();
    }

    // Reload hotbar slots for all players
    public void reloadHotbarSlots() {
        stationaryItems.clear();
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!isHotbarDisabled(p)) { // Nur Spieler mit aktiven Hotbar-Items berücksichtigen
                updateHotbarItems(p);
            }
        }
    }

    // Load preferences from file
    private void loadPreferences() {
        if (!configFile.exists()) return;

        for (String key : config.getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            boolean disabled = config.getBoolean(key);
            if (disabled) {
                disabledHotbars.add(playerId);
            }
        }
    }

    // Save preferences to file
    private void savePreferences() {
        // Zunächst die Datei leeren
        config.getKeys(false).forEach(key -> config.set(key, null));

        // Neue Daten eintragen
        for (UUID playerId : disabledHotbars) {
            config.set(playerId.toString(), true);
        }

        // Speichern
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save hotbar preferences: " + e.getMessage());
        }
    }

    // Return true if stationary slot exists and execute
    public boolean stationaryExecute(int slot, Player p, ClickType click, boolean openPanel) {
        if (stationaryItems.get(p.getUniqueId()).list.containsKey(String.valueOf(slot))) {
            if (openPanel) {
                try {
                    if (!String.valueOf(plugin.nbt.getNBT(p.getInventory().getItem(slot), "CommandPanelsHotbar", "string")).split(":")[1].equals(String.valueOf(slot))) {
                        return false;
                    }
                } catch (Exception ex) {
                    return false;
                }
                Panel panel = stationaryItems.get(p.getUniqueId()).getPanel(String.valueOf(slot));
                if (!p.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm"))) {
                    return false;
                }
                if (!itemCheckExecute(p.getInventory().getItem(slot), p, false, false)) {
                    return false;
                }
                if (panel.getHotbarSection(p).contains("commands")) {
                    plugin.commandRunner.runCommands(panel, PanelPosition.Top, p, panel.getHotbarSection(p).getStringList("commands"), click);
                    return true;
                }
                panel.open(p, PanelPosition.Top);
            }
            return true;
        }
        return false;
    }

    // Check and execute item
    public boolean itemCheckExecute(ItemStack invItem, Player p, boolean openPanel, boolean stationaryOnly) {
        if (!isValidHotbarItem(invItem, "CommandPanelsHotbar")) {
            return false;
        }
        for (Panel panel : plugin.panelList) {
            if (stationaryOnly) {
                try {
                    String nbtData = String.valueOf(plugin.nbt.getNBT(invItem, "CommandPanelsHotbar", "string"));
                    if (nbtData.split(":")[1].equals("-1")) {
                        continue;
                    }
                } catch (Exception ignore) {}
            }
            if (panel.hasHotbarItem()) {
                if (String.valueOf(plugin.nbt.getNBT(invItem, "CommandPanelsHotbar", "string")).split(":")[0].equals(panel.getName())) {
                    if (openPanel) {
                        if (!plugin.panelPerms.isPanelWorldEnabled(p, panel.getConfig())) {
                            return false;
                        }
                        if (panel.getHotbarSection(p).contains("commands")) {
                            for (String command : panel.getHotbarSection(p).getStringList("commands")) {
                                plugin.commandRunner.runCommand(panel, PanelPosition.Top, p, command);
                            }
                            return true;
                        }
                        panel.open(p, PanelPosition.Top);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
