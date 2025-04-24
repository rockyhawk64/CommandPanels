package me.rockyhawk.commandpanels.api;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.formatter.placeholders.PanelPlaceholders;
import me.rockyhawk.commandpanels.manager.PanelOpenType;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Panel{
    CommandPanels plugin = JavaPlugin.getPlugin(CommandPanels.class);
    /*This is the Panel object*/

    private ConfigurationSection panelConfig;
    private String panelName;
    private File panelFile = null;
    public PanelPlaceholders placeholders = new PanelPlaceholders();
    public boolean isOpen = false;

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
        String section = plugin.ctx.has.hasSection(this,PanelPosition.Top,panelConfig.getConfigurationSection("item." + slot), p);
        ConfigurationSection itemSection = panelConfig.getConfigurationSection("item." + slot + section);
        return plugin.ctx.itemCreate.makeItemFromConfig(this,PanelPosition.Top,itemSection, p, true, true, false);
    }

    public ItemStack getCustomItem(Player p, String itemName){
        String section = plugin.ctx.has.hasSection(this,PanelPosition.Top,panelConfig.getConfigurationSection("custom-item." + itemName), p);
        ConfigurationSection itemSection = panelConfig.getConfigurationSection("custom-item." + itemName + section);
        return plugin.ctx.itemCreate.makeCustomItemFromConfig(this,PanelPosition.Top,itemSection, p, true, true, false);
    }

    //NBT will equal to panelName:slot and the slot will be -1 if item is not stationery
    public ItemStack getHotbarItem(Player p){
        if (this.getConfig().contains("open-with-item.pre-load-commands")) {
            try {
                plugin.ctx.commandRunner.runCommands(this,PanelPosition.Top,p, this.getConfig().getStringList("open-with-item.pre-load-commands"), null);
            }catch(Exception s){
                plugin.ctx.debug.send(s,p, plugin.ctx);
            }
        }
        ItemStack s = plugin.ctx.itemCreate.makeItemFromConfig(this,PanelPosition.Top,getHotbarSection(p), p, true, true, false);
        String slot = "-1";
        if(getHotbarSection(p).isSet("stationary")){
            slot = getHotbarSection(p).getString("stationary");
        }
        try {
            //add NBT to item and return the ItemStack
            return plugin.ctx.nbt.setNBT(s, "CommandPanelsHotbar", panelName + ":" + slot);
        }catch(Exception e) {
            //return air if null
            return new ItemStack(Material.AIR);
        }
    }
    public ConfigurationSection getHotbarSection(Player p){
        String section = plugin.ctx.has.hasSection(this,PanelPosition.Top,panelConfig.getConfigurationSection("open-with-item"), p);
        return panelConfig.getConfigurationSection("open-with-item" + section);
    }

    public boolean hasHotbarItem(){
        return this.panelConfig.contains("open-with-item");
    }

    //this will make a preview of the inventory using a certain player on the top
    public Inventory getInventory(Player p){
        return plugin.ctx.createGUI.openGui(this,p,PanelPosition.Top, PanelOpenType.Return,0);
    }

    //open the panel for the player
    public void open(Player p, PanelPosition position){
        isOpen = true;
        plugin.ctx.openPanel.open(p, p, this, position);
    }

    //create blank clone
    public Panel copy(){
        if(panelFile != null){
            return new Panel(panelFile, panelName);
        }
        return new Panel(panelConfig, panelName);
    }
}
