package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class HotbarItemLoader {
    CommandPanels plugin;
    public HotbarItemLoader(CommandPanels pl) {
        this.plugin = pl;
    }

    //stationary slots 0-8 are the hotbar, using 9-27 for inside the inventory
    ArrayList<int[]> stationaryItems = new ArrayList<>(); //{slot 0-33, index of panelNames}

    //will compile the ArrayList {slot 0-4, index of panelNames}
    public void reloadHotbarSlots() {
        stationaryItems = new ArrayList<>();
        int i = 0;
        for (String[] panelName : plugin.panelNames) {
            ConfigurationSection tempFile = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(panelName[1])))).getConfigurationSection("panels." + panelName[0]);
            if(tempFile.contains("open-with-item.stationary")){
                stationaryItems.add(new int[]{tempFile.getInt("open-with-item.stationary"),i});
            }
            i++;
        }
    }

    //return true if found
    public boolean stationaryExecute(int slot, Player p, boolean openPanel){
        for(int[] temp : stationaryItems){
            if(slot == temp[0]){
                if(openPanel) {
                    String panelName = plugin.panelNames.get(temp[1])[0];
                    ConfigurationSection tempFile = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(plugin.panelNames.get(temp[1])[1])))).getConfigurationSection("panels." + panelName);
                    if (plugin.openPanels.hasPanelOpen(p.getName())) {
                        plugin.openPanels.skipPanels.add(p.getName());
                    }
                    plugin.openVoids.openCommandPanel(p, p, panelName, tempFile, false);
                }
                return true;
            }
        }
        return false;
    }

    //return true if found
    public boolean itemCheckExecute(ItemStack invItem, Player p, boolean openPanel){
        for(String[] panelName : plugin.panelNames) {
            ConfigurationSection tempFile = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(panelName[1])))).getConfigurationSection("panels." + panelName[0]);
            String tempName = panelName[0];
            if(tempFile.contains("open-with-item")){
                ItemStack panelItem = plugin.itemCreate.makeItemFromConfig(Objects.requireNonNull(tempFile.getConfigurationSection("open-with-item")), p, false, true);
                if(invItem != null && panelItem != null) {
                    panelItem.setAmount(invItem.getAmount());
                }else{
                    return false;
                }
                if(panelItem.isSimilar(invItem)){
                    if(openPanel) {
                        if (plugin.openPanels.hasPanelOpen(p.getName())) {
                            plugin.openPanels.skipPanels.add(p.getName());
                        }
                        plugin.openVoids.openCommandPanel(p, p, tempName, tempFile, false);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}