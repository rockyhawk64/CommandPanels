package me.rockyhawk.commandpanels;

import me.rockyhawk.commandpanels.commands.MainCommand;
import me.rockyhawk.commandpanels.commands.TabComplete;
import me.rockyhawk.commandpanels.formatter.Placeholders;
import me.rockyhawk.commandpanels.formatter.TextFormatter;
import me.rockyhawk.commandpanels.formatter.data.DataLoader;
import me.rockyhawk.commandpanels.interaction.openpanel.PanelOpenCommand;
import me.rockyhawk.commandpanels.session.SessionManager;
import me.rockyhawk.commandpanels.session.inventory.generator.GenerateManager;
import me.rockyhawk.commandpanels.session.inventory.listeners.ClickEvents;
import me.rockyhawk.commandpanels.session.inventory.listeners.InventoryEvents;
import org.bukkit.Bukkit;

public class Context {
    public CommandPanels plugin;
    public TextFormatter text;
    public FileHandler fileHandler;
    public PanelOpenCommand panelCommand;
    public DataLoader dataLoader;
    public SessionManager session;
    public GenerateManager generator;

    public Context(CommandPanels pl) {
        plugin = pl;
        init();
    }

    private void init(){
        text = new TextFormatter(this);
        fileHandler = new FileHandler(this);
        panelCommand = new PanelOpenCommand(this);
        dataLoader = new DataLoader(this);
        session = new SessionManager(this);
        generator = new GenerateManager(this);

        plugin.getCommand("panels").setExecutor(new MainCommand(this));
        plugin.getCommand("panels").setTabCompleter(new TabComplete(this));
        Bukkit.getServer().getPluginManager().registerEvents(session, plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new InventoryEvents(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(panelCommand, plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new ClickEvents(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(generator, plugin);

        // Register PlaceholderAPI in run task to ensure initialisation is complete
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                new Placeholders(this).register();
            }
        });
    }
}
