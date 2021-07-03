package me.rockyhawk.commandpanels.api;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.classresources.placeholders.PanelPlaceholders;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Panel{
    CommandPanels plugin = JavaPlugin.getPlugin(CommandPanels.class);
    /*This is the PanelConfig object*/

    private ConfigurationSection panelConfig;
    private String panelName;
    private File panelFile;
    public PanelPlaceholders placeholders = new PanelPlaceholders();

    //make the object, using a file is recommended
    public Panel(File file, String name){
        this.panelName = name;
        this.panelFile = file;
        this.panelConfig = YamlConfiguration.loadConfiguration(file).getConfigurationSection("panels." + name);
    }
    public Panel(ConfigurationSection config, String name){
        if(config.contains("panels")){
            config = config.getConfigurationSection("panels." + name);
        }
        this.panelName = name;
        this.panelConfig = config;
    }
    public Panel(String name){
        this.panelName = name;
    }

    //set elements of the panel
    public void setName(String name){
        this.panelName = name;
    }

    public void setConfig(ConfigurationSection config){
        if(config.contains("panels")){
            config = config.getConfigurationSection("panels." + this.panelName);
        }
        this.panelConfig = config;
    }

    public void setFile(File file){
        this.panelFile = file;
        this.panelConfig = YamlConfiguration.loadConfiguration(file).getConfigurationSection("panels." + this.getName());
    }

    //get elements of the panel
    public String getName(){
        return this.panelName;
    }

    public ConfigurationSection getConfig(){
        return this.panelConfig;
    }

    public File getFile(){
        return this.panelFile;
    }

    public ItemStack getItem(Player p, int slot){
        ConfigurationSection itemSection = panelConfig.getConfigurationSection("item." + slot);
        return plugin.itemCreate.makeItemFromConfig(this,itemSection, p, true, true, false);
    }

    public ItemStack getCustomItem(Player p, String itemName){
        ConfigurationSection itemSection = panelConfig.getConfigurationSection("custom-item." + itemName);
        return plugin.itemCreate.makeCustomItemFromConfig(this,itemSection, p, true, true, false);
    }

    public ItemStack getHotbarItem(Player p){
        ConfigurationSection itemSection = panelConfig.getConfigurationSection("open-with-item");
        ItemStack s = plugin.itemCreate.makeItemFromConfig(this,itemSection, p, true, true, false);
        return plugin.nbt.setNBT(s,"CommandPanelsHotbar",panelName);
    }

    public boolean hasHotbarItem(){
        return this.panelConfig.contains("open-with-item");
    }

    //this will make a preview of the inventory using a certain player
    public Inventory getInventory(Player p){
        return plugin.createGUI.openGui(this,p,2,0);
    }

    //open the panel for the player, it will disable debug mode
    public void open(Player p){
        if(plugin.debug.isEnabled(p)){
            plugin.debug.debugSet.remove(p);
        }
        plugin.openVoids.openCommandPanel(p, p, this, false);
    }

    //create blank clone
    public Panel copy(){
        if(panelFile != null){
            return new Panel(panelFile, panelName);
        }
        return new Panel(panelConfig, panelName);
    }
}
