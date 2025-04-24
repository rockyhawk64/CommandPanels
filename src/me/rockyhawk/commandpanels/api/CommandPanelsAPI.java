package me.rockyhawk.commandpanels.api;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CommandPanelsAPI {
    //Public using CommandPanels and not Context for API usage
    public Context ctx;

    public CommandPanelsAPI(Context pl) {
        this.ctx = pl;
    }

    //returns true if the player has a panel open
    public boolean isPanelOpen(Player p){
        return ctx.openPanels.hasPanelOpen(p.getName(),PanelPosition.Top);
    }

    //get the name of a panel currently open, will return null if panel is not open
    public Panel getOpenPanel(Player p, PanelPosition position){
        return ctx.openPanels.getOpenPanel(p.getName(), position);
    }

    //loaded panels in folder
    public List<Panel> getPanelsLoaded(){
        return ctx.plugin.panelList;
    }

    //import panel into folder
    public void addPanel(Panel panel) throws IOException{
        File addedFile = new File(ctx.configHandler.panelsFolder, panel.getName() + ".yml");
        YamlConfiguration newYaml = new YamlConfiguration();
        if(panel.getConfig().contains("panels")){
            newYaml.set("",panel.getConfig());
        }else{
            newYaml.set("panels." + panel.getName(),panel.getConfig());
        }
        newYaml.save(addedFile);
        ctx.reloader.reloadPanelFiles();
    }

    //remove panel from folder
    public void removePanel(Panel panel){
        for(Panel panels : ctx.plugin.panelList){
            if(panels.getName().equals(panel.getName())){
                if(panels.getFile().delete()){
                    ctx.reloader.reloadPanelFiles();
                }
            }
        }
    }

    //get panel from folder
    public Panel getPanel(String panelName){
        for(Panel panel : ctx.plugin.panelList) {
            if(panel.getName().equals(panelName)) {
                return panel;
            }
        }
        return null;
    }

    //if the players inventory has no panels in it
    public boolean hasNormalInventory(Player p){
        return ctx.inventorySaver.hasNormalInventory(p);
    }

    //make custom item using items section
    public ItemStack makeItem(Player p, ConfigurationSection itemSection){
        return ctx.itemCreate.makeCustomItemFromConfig(null,PanelPosition.Top,itemSection, p, true, true, false);
    }
}