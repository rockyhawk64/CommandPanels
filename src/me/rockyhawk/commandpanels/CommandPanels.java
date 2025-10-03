package me.rockyhawk.commandpanels;

import me.rockyhawk.commandpanels.session.Panel;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;

public class CommandPanels extends JavaPlugin {
    public File folder = new File(this.getDataFolder() + File.separator + "panels");
    public HashMap<String, Panel> panels = new HashMap<>(); // String is for an index by panel name

    public Context ctx;

    private static final Logger logger = LoggerFactory.getLogger(CommandPanels.class);

    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("[CommandPanels] RockyHawk's CommandPanels v" + this.getPluginMeta().getVersion() + " Plugin Loading...");

        try {
            // Register plugin default permissions
            registerPermissions();

            //Initialise plugin context
            ctx = new Context(this);

            //add custom charts bStats
            Metrics metrics = new Metrics(this, 5097);
            metrics.addCustomChart(new SingleLineChart("panels_amount", () -> {
                //this is the total panels loaded
                return panels.size();
            }));

            Bukkit.getConsoleSender().sendMessage("[CommandPanels] RockyHawk's CommandPanels v" + this.getPluginMeta().getVersion() + " Plugin Loaded!");
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("[CommandPanels] Failed to load plugin: " + e.getMessage());
            logger.error("Plugin loading failed with exception", e);
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

    // Plugin registration methods
    private void registerPermissions() {
        addPermission("commandpanels.command", PermissionDefault.TRUE);
        addPermission("commandpanels.command.reload", PermissionDefault.OP);
        addPermission("commandpanels.command.generate", PermissionDefault.OP);
        addPermission("commandpanels.command.version", PermissionDefault.TRUE);
        addPermission("commandpanels.command.data", PermissionDefault.OP);
        addPermission("commandpanels.command.open", PermissionDefault.OP);
        addPermission("commandpanels.command.open.other", PermissionDefault.OP);
        addPermission("commandpanels.command.help", PermissionDefault.TRUE);
    }

    private void addPermission(String name, PermissionDefault defaultValue) {
        Permission permission = new Permission(name, defaultValue);
        if (getServer().getPluginManager().getPermission(name) == null) {
            getServer().getPluginManager().addPermission(permission);
        }
    }
}