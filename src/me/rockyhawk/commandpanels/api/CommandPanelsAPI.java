package me.rockyhawk.commandpanels.api;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class CommandPanelsAPI {
    CommandPanels plugin;
    public CommandPanelsAPI(CommandPanels pl) {
        this.plugin = pl;
    }

    //returns true if the player has a panel open
    public boolean isPanelOpen(Player p){
        return plugin.openPanels.hasPanelOpen(p.getName());
    }

    //get the name of a panel currently open, will return null if panel is not open
    public Panel getOpenPanel(Player p){
        return plugin.openPanels.getOpenPanel(p.getName());
    }

    //loaded panels in folder
    public List<Panel> getPanelsLoaded(){
        return plugin.panelList;
    }

    //import panel into folder
    public void addPanel(Panel panel) throws IOException{
        File addedFile = new File(plugin.panelsf + File.separator + panel.getFile().getName());
        YamlConfiguration addedYaml = YamlConfiguration.loadConfiguration(panel.getFile());
        addedYaml.save(addedFile);
        plugin.reloadPanelFiles();
    }

    //remove panel from folder
    public void removePanel(Panel panel){
        for(Panel panels : plugin.panelList){
            if(panels.getName().equals(panel.getName())){
                if(panels.getFile().delete()){
                    plugin.reloadPanelFiles();
                }
            }
        }
    }

    //get panel from folder
    public Panel getPanel(String panelName){
        for(Panel panel : plugin.panelList) {
            if(panel.getName().equals(panelName)) {
                return panel;
            }
        }
        return null;
    }

    //make custom item using items section
    public ItemStack makeItem(Player p, ConfigurationSection itemSection){
        return plugin.itemCreate.makeCustomItemFromConfig(itemSection, p, true, true, false);
    }

    //will return item slots of hotbar stationary items
    public Set<Integer> getHotbarItems(){
        return plugin.hotbar.getStationaryItemSlots();
    }
}