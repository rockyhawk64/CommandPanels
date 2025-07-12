package me.rockyhawk.commandpanels;

import me.rockyhawk.commandpanels.session.Panel;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class CommandPanels extends JavaPlugin{
    public File folder = new File(this.getDataFolder() + File.separator + "panels");
    public HashMap<String, Panel> panels = new HashMap<>(); // String is for an index by panel name

    public Context ctx;

    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("[CommandPanels] RockyHawk's CommandPanels v" + this.getDescription().getVersion() + " Plugin Loading...");

        try {
            //Initialise plugin context
            ctx = new Context(this);

            //add custom charts bStats
            Metrics metrics = new Metrics(this, 5097);
            metrics.addCustomChart(new SingleLineChart("panels_amount", () -> {
                //this is the total panels loaded
                return panels.size();
            }));

            Bukkit.getConsoleSender().sendMessage("[CommandPanels] RockyHawk's CommandPanels v" + this.getDescription().getVersion() + " Plugin Loaded!");
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("[CommandPanels] Failed to load plugin: " + e.getMessage());
            e.printStackTrace();
            // Set context to null to prevent issues in onDisable
            ctx = null;
        }
    }

    public void onDisable() {
        // Check if context was properly initialized before attempting cleanup
        if (ctx == null) {
            Bukkit.getConsoleSender().sendMessage("[CommandPanels] RockyHawk's CommandPanels Plugin Disabled (initialization failed).");
            return;
        }

        try {
            // Code on plugin disable
            ctx.dataLoader.saveDataFileSync();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("[CommandPanels] Error during CommandPanels disable: " + e.getMessage());
        }

        Bukkit.getConsoleSender().sendMessage("[CommandPanels] RockyHawk's CommandPanels Plugin Disabled, aww man.");
    }
}