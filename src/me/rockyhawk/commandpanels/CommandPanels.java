package me.rockyhawk.commandpanels;

import me.rockyhawk.commandpanels.api.CommandPanelsAPI;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.*;
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

        //Initialise plugin context
        ctx = new Context(this);

        //add custom charts bStats
        Metrics metrics = new Metrics(this, 5097);
        metrics.addCustomChart(new SingleLineChart("panels_amount", () -> {
            //this is the total panels loaded
            return panelList.size();
        }));

        Bukkit.getLogger().info("[CommandPanels] RockyHawk's CommandPanels v" + this.getDescription().getVersion() + " Plugin Loaded!");
    }

    public void onDisable() {
        //close all the panels
        for(String name : ctx.openPanels.openPanels.keySet()){
            ctx.openPanels.closePanelForLoader(name, PanelPosition.Top);
            try {
                Bukkit.getPlayer(name).closeInventory();
            }catch (Exception ignore){}
        }

        //save files
        ctx.panelData.saveDataFile();
        ctx.inventorySaver.saveInventoryFile();
        ctx.updater.autoUpdatePlugin(this.getFile().getName());
        Bukkit.getLogger().info("RockyHawk's CommandPanels Plugin Disabled, aww man.");
    }

    public static CommandPanelsAPI getAPI(){
        return new CommandPanelsAPI(JavaPlugin.getPlugin(CommandPanels.class).ctx);
    }
}
