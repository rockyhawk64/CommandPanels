package me.rockyhawk.commandpanels;

import me.rockyhawk.commandpanels.api.CommandPanelsAPI;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class CommandPanels extends JavaPlugin{
    public boolean openWithItem = false; //this will be true if there is a panel with open-with-item
    public List<Panel> panelList = new ArrayList<>(); //contains all the panels that are included in the panels folder

    public Context ctx;

    public void onEnable() {
        Bukkit.getLogger().info("[CommandPanels] RockyHawk's CommandPanels v" + this.getDescription().getVersion() + " Plugin Loading...");

        try {
            //Initialise plugin context
            ctx = new Context(this);

            //add custom charts bStats
            Metrics metrics = new Metrics(this, 5097);
            metrics.addCustomChart(new SingleLineChart("panels_amount", () -> {
                //this is the total panels loaded
                return panelList.size();
            }));

            Bukkit.getLogger().info("[CommandPanels] RockyHawk's CommandPanels v" + this.getDescription().getVersion() + " Plugin Loaded!");
        } catch (Exception e) {
            Bukkit.getLogger().severe("[CommandPanels] Failed to load plugin: " + e.getMessage());
            e.printStackTrace();
            // Set ctx to null to prevent issues in onDisable
            ctx = null;
            throw e; // Re-throw to properly disable the plugin
        }
    }

    public void onDisable() {
        // Check if context was properly initialized before attempting cleanup
        if (ctx == null) {
            Bukkit.getLogger().info("RockyHawk's CommandPanels Plugin Disabled (initialization failed).");
            return;
        }

        try {
            //stop autosave system
            if (ctx.autoSave != null) {
                ctx.autoSave.stop();
            }
            
            //close all the panels
            if (ctx.openPanels != null && ctx.openPanels.openPanels != null) {
                for(String name : ctx.openPanels.openPanels.keySet()){
                    ctx.openPanels.closePanelForLoader(name, PanelPosition.Top);
                    try {
                        Bukkit.getPlayer(name).closeInventory();
                    }catch (Exception ignore){}
                }
            }

            //save files
            if (ctx.panelData != null) {
                ctx.panelData.saveDataFile();
            }
            if (ctx.inventorySaver != null) {
                ctx.inventorySaver.saveInventoryFile();
            }
            if (ctx.updater != null) {
                ctx.updater.autoUpdatePlugin(this.getFile().getName());
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error during CommandPanels shutdown: " + e.getMessage());
        }
        
        Bukkit.getLogger().info("RockyHawk's CommandPanels Plugin Disabled, aww man.");
    }

    public static CommandPanelsAPI getAPI(){
        return new CommandPanelsAPI(JavaPlugin.getPlugin(CommandPanels.class).ctx);
    }
}
