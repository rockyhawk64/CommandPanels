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
import java.util.Objects;
import java.util.UUID;

public class HotbarItemLoader {
    private final CommandPanels plugin;

    // Stationary slots map for managing items per player
    private final HashMap<UUID, HotbarPlayerManager> stationaryItems = new HashMap<>();

    public HotbarItemLoader(CommandPanels plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads all hotbar slots for online players.
     * Clears the stationary items map and updates hotbars for all online players.
     */
    public void reloadHotbarSlots() {
        stationaryItems.clear();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            updateHotbarItems(player);
        }
    }

    /**
     * Executes an action for a stationary slot.
     * 
     * @param slot The slot number.
     * @param player The player interacting with the slot.
     * @param click The type of click interaction.
     * @param openPanel Whether the panel should be opened.
     * @return True if the action was successfully executed, false otherwise.
     */
    public boolean stationaryExecute(int slot, Player player, ClickType click, boolean openPanel) {
        HotbarPlayerManager manager = stationaryItems.get(player.getUniqueId());
        if (manager != null && manager.list.containsKey(String.valueOf(slot))) {
            if (openPanel) {
                try {
                    ItemStack item = player.getInventory().getItem(slot);
                    String nbtData = String.valueOf(plugin.nbt.getNBT(item, "CommandPanelsHotbar", "string"));
                    if (!nbtData.split(":")[1].equals(String.valueOf(slot))) {
                        return false;
                    }
                } catch (Exception ex) {
                    return false;
                }

                Panel panel = manager.getPanel(String.valueOf(slot));
                if (!player.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm"))) {
                    return false;
                }
                if (!itemCheckExecute(player.getInventory().getItem(slot), player, false, false)) {
                    return false;
                }

                if (panel.getHotbarSection(player).contains("commands")) {
                    plugin.commandRunner.runCommands(panel, PanelPosition.Top, player, panel.getHotbarSection(player).getStringList("commands"), click);
                } else {
                    panel.open(player, PanelPosition.Top);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Checks and executes an action for a hotbar item.
     * 
     * @param item The item to check.
     * @param player The player interacting with the item.
     * @param openPanel Whether the panel should be opened.
     * @param stationaryOnly Whether to restrict to stationary items.
     * @return True if the item is valid and the action was executed, false otherwise.
     */
    public boolean itemCheckExecute(ItemStack item, Player player, boolean openPanel, boolean stationaryOnly) {
        try {
            String nbtData = String.valueOf(plugin.nbt.getNBT(item, "CommandPanelsHotbar", "string"));
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
                            plugin.commandRunner.runCommands(panel, PanelPosition.Top, player, panel.getHotbarSection(player).getStringList("commands"), null);
                        } else {
                            panel.open(player, PanelPosition.Top);
                        }
                    }
                    return true;
                }
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    /**
     * Updates the hotbar items for a player.
     * Removes old items and adds new ones based on the panel configuration.
     * 
     * @param player The player whose hotbar items are to be updated.
     */
    public void updateHotbarItems(Player player) {
        if (!plugin.openWithItem) {
            return; // Skip if the feature is disabled
        }

        stationaryItems.put(player.getUniqueId(), new HotbarPlayerManager());

        // Remove old hotbar items
        for (int i = 0; i <= 35; i++) {
            try {
                ItemStack item = player.getInventory().getItem(i);
                String nbtData = String.valueOf(plugin.nbt.getNBT(item, "CommandPanelsHotbar", "string"));
                if (nbtData != null && !nbtData.endsWith("-1")) {
                    player.getInventory().setItem(i, new ItemStack(Material.AIR));
                }
            } catch (Exception ignore) {
            }
        }

        // Add new hotbar items
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
}
