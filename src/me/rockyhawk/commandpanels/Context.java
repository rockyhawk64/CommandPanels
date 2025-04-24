package me.rockyhawk.commandpanels;

import me.rockyhawk.commandpanels.interaction.click.InteractionHandler;
import me.rockyhawk.commandpanels.manager.open.OpenPanel;
import me.rockyhawk.commandpanels.items.HasSections;
import me.rockyhawk.commandpanels.items.ItemCreation;
import me.rockyhawk.commandpanels.formatter.MiniMessage;
import me.rockyhawk.commandpanels.items.customheads.GetCustomHeads;
import me.rockyhawk.commandpanels.items.dropitem.DroppedItemHandler;
import me.rockyhawk.commandpanels.formatter.CreateText;
import me.rockyhawk.commandpanels.formatter.HexColours;
import me.rockyhawk.commandpanels.formatter.placeholders.Placeholders;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderAPI;
import me.rockyhawk.commandpanels.commands.*;
import me.rockyhawk.commandpanels.interaction.commands.CommandRunner;
import me.rockyhawk.commandpanels.commands.tabcomplete.PanelTabComplete;
import me.rockyhawk.commandpanels.commands.tabcomplete.DataTabComplete;
import me.rockyhawk.commandpanels.commands.tabcomplete.ImportTabComplete;
import me.rockyhawk.commandpanels.commands.tabcomplete.UpdateTabComplete;
import me.rockyhawk.commandpanels.configuration.ConfigHandler;
import me.rockyhawk.commandpanels.commands.opencommands.OpenCommands;
import me.rockyhawk.commandpanels.configuration.DebugManager;
import me.rockyhawk.commandpanels.formatter.data.DataLoader;
import me.rockyhawk.commandpanels.formatter.data.DataManager;
import me.rockyhawk.commandpanels.generate.deluxecompatibility.CompatibilityConverter;
import me.rockyhawk.commandpanels.commands.editor.CommandPanelsEditor;
import me.rockyhawk.commandpanels.commands.editor.EditorTabComplete;
import me.rockyhawk.commandpanels.commands.editor.PanelDownloader;
import me.rockyhawk.commandpanels.builder.floodgate.OpenFloodgateGUI;
import me.rockyhawk.commandpanels.generate.GenerateCommand;
import me.rockyhawk.commandpanels.generate.GenUtils;
import me.rockyhawk.commandpanels.generate.GenTabComplete;
import me.rockyhawk.commandpanels.manager.refresh.PanelRefresher;
import me.rockyhawk.commandpanels.manager.OpenEvent;
import me.rockyhawk.commandpanels.interaction.click.OutsideHandler;
import me.rockyhawk.commandpanels.interaction.input.PlayerInputUtils;
import me.rockyhawk.commandpanels.versions.VersionManager;
import me.rockyhawk.commandpanels.nbt.NBTManager;
import me.rockyhawk.commandpanels.items.potions.ClassicPotionData;
import me.rockyhawk.commandpanels.items.potions.LegacyPotionData;
import me.rockyhawk.commandpanels.builder.OpenGUI;
import me.rockyhawk.commandpanels.manager.session.SessionHandler;
import me.rockyhawk.commandpanels.manager.session.SessionUtils;
import me.rockyhawk.commandpanels.openwithitem.HotbarItemLoader;
import me.rockyhawk.commandpanels.openwithitem.events.SwapItemEvent;
import me.rockyhawk.commandpanels.openwithitem.events.UtilsChestSortEvent;
import me.rockyhawk.commandpanels.openwithitem.events.HotbarEvents;
import me.rockyhawk.commandpanels.interaction.blocks.BlockTabComplete;
import me.rockyhawk.commandpanels.interaction.blocks.BlockCommand;
import me.rockyhawk.commandpanels.interaction.blocks.BlockEvents;
import me.rockyhawk.commandpanels.inventory.InventorySaver;
import me.rockyhawk.commandpanels.inventory.ItemStackSerializer;
import me.rockyhawk.commandpanels.inventory.pickupevent.EntityPickupEvent;
import me.rockyhawk.commandpanels.inventory.pickupevent.LegacyPlayerEvent;
import me.rockyhawk.commandpanels.updater.Updater;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Objects;

public class Context {
    public CommandPanels plugin;
    public String tag = "[CommandPanels]";

    //get plugin classes
    public PanelDownloader downloader;
    public ConfigHandler configHandler;
    public Economy econ;
    public ReloadCommand reloader;

    public CommandRunner commandRunner;
    public DataLoader panelData;
    public DataManager panelDataPlayers;

    public OpenCommands openCommands;
    public Placeholders placeholders;
    public DebugManager debug;
    public CreateText text;
    public HexColours hex;

    public MiniMessage miniMessage;

    public OpenPanel openPanel;
    public ItemCreation itemCreate;
    public HasSections has;
    public GetCustomHeads customHeads;
    public Updater updater;
    public ClassicPotionData classicPotion;
    public LegacyPotionData legacyPotion;
    public VersionManager version;

    public SessionHandler openPanels;
    public OpenGUI createGUI;
    public HotbarItemLoader hotbar;
    public NBTManager nbt;

    public GenUtils generator;
    public InventorySaver inventorySaver;
    public ItemStackSerializer itemSerializer;
    public PlayerInputUtils inputUtils;
    public CompatibilityConverter deluxeConverter;

    public Context(CommandPanels pl) {
        plugin = pl;
        init();
    }

    @SuppressWarnings("DataFlowIssue")
    private void init(){
        //get plugin classes
        downloader = new PanelDownloader(this);
        panelData = new DataLoader(this);
        inventorySaver = new InventorySaver(this);
        configHandler = new ConfigHandler(this);
        version = new VersionManager();
        econ = null;

        deluxeConverter = new CompatibilityConverter(this);
        reloader = new ReloadCommand(this);

        commandRunner = new CommandRunner(this);
        panelDataPlayers = new DataManager();

        placeholders = new Placeholders(this);
        debug = new DebugManager();
        text = new CreateText(this);
        hex = new HexColours(this);

        miniMessage = null;

        openPanel = new OpenPanel(this);
        itemCreate = new ItemCreation(this);
        has = new HasSections(this);
        customHeads = new GetCustomHeads(this);
        updater = new Updater(this);
        classicPotion = new ClassicPotionData();
        legacyPotion = new LegacyPotionData();

        openCommands = new OpenCommands(this);
        openPanels = new SessionHandler(this);
        createGUI = new OpenGUI(this);
        hotbar = new HotbarItemLoader(this);
        nbt = new NBTManager(this);

        generator = new GenUtils(this);
        itemSerializer = new ItemStackSerializer(this);
        inputUtils = new PlayerInputUtils(this);

        tag = text.colour(configHandler.config.getString("config.format.tag"));

        //setup class files
        setupEconomy();
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getCommand("commandpanel").setExecutor(new PanelCommand(this));
        plugin.getCommand("commandpanel").setTabCompleter(new PanelTabComplete(this));

        plugin.getCommand("commandpanelgenerate").setTabCompleter(new GenTabComplete());
        plugin.getCommand("commandpanelgenerate").setExecutor(new GenerateCommand(this));

        plugin.getCommand("commandpaneldata").setTabCompleter(new DataTabComplete(this));
        plugin.getCommand("commandpaneldata").setExecutor(new DataCommand(this));

        plugin.getCommand("commandpanelupdate").setTabCompleter(new UpdateTabComplete());
        plugin.getCommand("commandpanelupdate").setExecutor(new UpdateCommand(this));

        plugin.getCommand("commandpanelimport").setExecutor(new ImportCommand(this));
        plugin.getCommand("commandpanelimport").setTabCompleter(new ImportTabComplete(this));

        plugin.getCommand("commandpaneledit").setExecutor(new CommandPanelsEditor(this));
        plugin.getCommand("commandpaneledit").setTabCompleter(new EditorTabComplete(this));

        plugin.getCommand("commandpanelreload").setExecutor(reloader);
        plugin.getCommand("commandpaneldebug").setExecutor(new DebugCommand(this));
        plugin.getCommand("commandpanelversion").setExecutor(new VersionCommand(this));
        plugin.getCommand("commandpanellist").setExecutor(new ListCommand(this));

        if(this.version.isAtLeast("1.12")){
            Bukkit.getServer().getPluginManager().registerEvents(new EntityPickupEvent(this), plugin);
        }else{
            Bukkit.getServer().getPluginManager().registerEvents(new LegacyPlayerEvent(this), plugin);
        }

        try {
            // Check all the minimessage classes exist before loading
            Class.forName("net.kyori.adventure.text.Component");
            Class.forName("net.kyori.adventure.text.format.TextDecoration");
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
            miniMessage = new MiniMessage(this);
        } catch (ClassNotFoundException ignore) {
            //do not initialise miniMessage
        }

        Bukkit.getServer().getPluginManager().registerEvents(new InteractionHandler(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(inventorySaver, plugin);
        Bukkit.getServer().getPluginManager().registerEvents(inputUtils, plugin);
        Bukkit.getServer().getPluginManager().registerEvents(panelDataPlayers, plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new SessionUtils(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(generator, plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new DroppedItemHandler(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new OpenEvent(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new OutsideHandler(this), plugin);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("floodgate")) {
            Bukkit.getServer().getPluginManager().registerEvents(new OpenFloodgateGUI(this), plugin);
        }
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPI(this).register();
        }
        if (configHandler.isTrue("updater.update-checks")) {
            Bukkit.getServer().getPluginManager().registerEvents(updater, plugin);
        }
        if(configHandler.isTrue("config.refresh-panels")){
            Bukkit.getServer().getPluginManager().registerEvents(new PanelRefresher(this), plugin);
        }
        if(configHandler.isTrue("config.custom-commands")){
            Bukkit.getServer().getPluginManager().registerEvents(openCommands, plugin);
        }
        if(configHandler.isTrue("config.hotbar-items")){
            Bukkit.getServer().getPluginManager().registerEvents(new HotbarEvents(this), plugin);
        }
        if(configHandler.isTrue("config.panel-blocks")){
            Objects.requireNonNull(plugin.getCommand("commandpanelblock")).setExecutor(new BlockCommand(this));
            Objects.requireNonNull(plugin.getCommand("commandpanelblock")).setTabCompleter(new BlockTabComplete(this));
            Bukkit.getServer().getPluginManager().registerEvents(new BlockEvents(this), plugin);
        }
        if (!Bukkit.getVersion().contains("1.8")) {
            Bukkit.getServer().getPluginManager().registerEvents(new SwapItemEvent(this), plugin);
        }
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("ChestSort")){
            Bukkit.getServer().getPluginManager().registerEvents(new UtilsChestSortEvent(this), plugin);
        }
    }

    private void setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                econ = rsp.getProvider();
            }
        }
    }
}
