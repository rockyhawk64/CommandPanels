package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class HotbarItemLoader {
    private final CommandPanels plugin;

    // Stationary slots map for managing items per player
    private final HashMap<UUID, HotbarPlayerManager> stationaryItems = new HashMap<>();

    public HotbarItemLoader(CommandPanels plugin) {
        this.plugin = plugin;
    }

    public HashMap<UUID, HotbarPlayerManager> getStationaryItems() {
        return stationaryItems;
    }

    /**
     * Überprüft, ob Hotbar-Items für den Spieler aktiviert sind.
     * @param player Der Spieler, dessen Status geprüft wird.
     * @return true, wenn Hotbar-Items aktiviert sind, andernfalls false.
     */
    public boolean isHotbarItemsEnabled(Player player) {
        return getStationaryItems().containsKey(player.getUniqueId());
    }

    /**
     * Reloads all hotbar slots for online players.
     * Clears the stationary items map and updates hotbars for all online players.
     */
    public void reloadHotbarSlots() {
        getStationaryItems().clear();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            updateHotbarItems(player);
        }
    }

    /**
     * Executes an action for a stationary slot.
     */
    public boolean stationaryExecute(int slot, Player player, ClickType click, boolean openPanel) {
        HotbarPlayerManager manager = getStationaryItems().get(player.getUniqueId());
        if (manager == null || !manager.list.containsKey(String.valueOf(slot))) {
            return false;
        }

        if (openPanel) {
            try {
                ItemStack item = player.getInventory().getItem(slot);
                String nbtData = getNBTData(item, "CommandPanelsHotbar");
                if (nbtData == null || !nbtData.split(":")[1].equals(String.valueOf(slot))) {
                    return false;
                }

                Panel panel = manager.getPanel(String.valueOf(slot));
                if (!player.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm")) 
                        || !itemCheckExecute(item, player, false, false)) {
                    return false;
                }

                if (panel.getHotbarSection(player).contains("commands")) {
                    plugin.commandRunner.runCommands(panel, PanelPosition.Top, player, 
                        panel.getHotbarSection(player).getStringList("commands"), click);
                } else {
                    panel.open(player, PanelPosition.Top);
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Error in stationaryExecute: ", ex);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks and executes an action for a hotbar item.
     */
    public boolean itemCheckExecute(ItemStack item, Player player, boolean openPanel, boolean stationaryOnly) {
        String nbtData = getNBTData(item, "CommandPanelsHotbar");
        if (nbtData == null || nbtData.isEmpty()) {
            return false;
        }

        for (Panel panel : plugin.panelList) {
            if (stationaryOnly && nbtData.split(":")[1].equals("-1")) {
                continue;
            }
            if (panel.hasHotbarItem() && nbtData.split(":")[0].equals(panel.getName())) {
                if (openPanel) {
                    if (!plugin.panelPerms.isPanelWorldEnabled(player, panel.getConfig())) {
                        return false;
                    }
                    if (panel.getHotbarSection(player).contains("commands")) {
                        plugin.commandRunner.runCommands(panel, PanelPosition.Top, player, 
                            panel.getHotbarSection(player).getStringList("commands"), null);
                    } else {
                        panel.open(player, PanelPosition.Top);
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the hotbar items for a player.
     */
public void updateHotbarItems(Player player) {
    if (!plugin.openWithItem || !isHotbarItemsEnabled(player)) {
        return;
    }

    getStationaryItems().put(player.getUniqueId(), new HotbarPlayerManager());

    for (int i = 0; i <= 35; i++) {
        try {
            ItemStack item = player.getInventory().getItem(i);
            String nbtData = getNBTData(item, "CommandPanelsHotbar");
            if (nbtData != null && !nbtData.endsWith("-1")) {
                player.getInventory().setItem(i, new ItemStack(Material.AIR));
            }
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Error removing hotbar items: ", ex);
        }
    }

    for (Panel panel : plugin.panelList) {
        if (!plugin.panelPerms.isPanelWorldEnabled(player, panel.getConfig()) 
                || !panel.hasHotbarItem() 
                || !player.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm"))) {
            continue;
        }

        ItemStack hotbarItem = panel.getHotbarItem(player);
        if (panel.getHotbarSection(player).contains("stationary")) {
            int slot = Integer.parseInt(panel.getHotbarSection(player).getString("stationary"));
            player.getInventory().setItem(slot, hotbarItem);
            getStationaryItems().get(player.getUniqueId()).addSlot(String.valueOf(slot), panel);
        }
    }

    player.updateInventory();
}

    public void toggleHotbarItems(Player player) {
        if (getStationaryItems().containsKey(player.getUniqueId())) {
            // Entferne alle Hotbar-Items des Spielers
            getStationaryItems().remove(player.getUniqueId());
            for (int i = 0; i <= 35; i++) {
                try {
                    // Entferne das Item aus der Hotbar, ohne NBT-Daten zu überprüfen
                    ItemStack item = player.getInventory().getItem(i);
                    if (item != null && item.getType() != Material.AIR) {
                        player.getInventory().setItem(i, new ItemStack(Material.AIR));
                    }
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, "Error toggling hotbar items: ", ex);
                }
            }
        } else {
            // Lade und setze die Hotbar-Items neu
            updateHotbarItems(player);
        }
    }    
    
    /**
     * Retrieves NBT data from an item.
     */
    public String getNBTData(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR || item.getAmount() <= 0) {
            // Wenn der ItemStack null oder leer ist, geben wir null zurück, aber ohne eine Warnung
            return null;
        }
    
        try {
            // Sicherstellen, dass NBT-Daten abgerufen werden können
            return (String) plugin.nbt.getNBT(item, key, "string");
        } catch (Exception ex) {
            // Fehlerprotokollierung, falls das Abrufen von NBT-Daten fehlschlägt
            plugin.getLogger().log(Level.WARNING, "Failed to retrieve NBT data for " + item.getType() + ": ", ex);
            return null;
        }
    }    
}
