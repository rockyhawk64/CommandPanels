package me.rockyhawk.commandpanels;

import me.rockyhawk.commandpanels.classresources.ExecuteOpenVoids;
import me.rockyhawk.commandpanels.classresources.HasSections;
import me.rockyhawk.commandpanels.classresources.ItemCreation;
import me.rockyhawk.commandpanels.classresources.MiniMessageUtils;
import me.rockyhawk.commandpanels.classresources.customheads.GetCustomHeads;
import me.rockyhawk.commandpanels.classresources.item_fall.ItemFallManager;
import me.rockyhawk.commandpanels.classresources.placeholders.CreateText;
import me.rockyhawk.commandpanels.classresources.placeholders.HexColours;
import me.rockyhawk.commandpanels.classresources.placeholders.Placeholders;
import me.rockyhawk.commandpanels.classresources.placeholders.expansion.PlaceholderAPI;
import me.rockyhawk.commandpanels.commands.*;
import me.rockyhawk.commandpanels.commandtags.CommandRunner;
import me.rockyhawk.commandpanels.completetabs.PanelTabComplete;
import me.rockyhawk.commandpanels.completetabs.DataTabComplete;
import me.rockyhawk.commandpanels.completetabs.ImportTabComplete;
import me.rockyhawk.commandpanels.completetabs.UpdateTabComplete;
import me.rockyhawk.commandpanels.configuration.ConfigHandler;
import me.rockyhawk.commandpanels.customcommands.PanelCommands;
import me.rockyhawk.commandpanels.datamanager.DebugManager;
import me.rockyhawk.commandpanels.datamanager.PanelDataLoader;
import me.rockyhawk.commandpanels.datamanager.PanelDataPlayerManager;
import me.rockyhawk.commandpanels.deluxecompatibility.CompatibilityConverter;
import me.rockyhawk.commandpanels.editor.CommandPanelsEditor;
import me.rockyhawk.commandpanels.editor.EditorTabComplete;
import me.rockyhawk.commandpanels.editor.PanelDownloader;
import me.rockyhawk.commandpanels.floodgate.OpenFloodgateGUI;
import me.rockyhawk.commandpanels.generatepanels.GenerateCommand;
import me.rockyhawk.commandpanels.generatepanels.GenUtils;
import me.rockyhawk.commandpanels.generatepanels.TabCompleteGenerate;
import me.rockyhawk.commandpanels.interactives.PanelRefresher;
import me.rockyhawk.commandpanels.interactives.OpenOnJoin;
import me.rockyhawk.commandpanels.interactives.OutsideClickEvent;
import me.rockyhawk.commandpanels.interactives.input.PlayerInputUtils;
import me.rockyhawk.commandpanels.ioclasses.legacy.LegacyVersion;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
import me.rockyhawk.commandpanels.ioclasses.legacy.PlayerHeads;
import me.rockyhawk.commandpanels.ioclasses.nbt.NBTManager;
import me.rockyhawk.commandpanels.ioclasses.potions.ClassicPotionData;
import me.rockyhawk.commandpanels.ioclasses.potions.LegacyPotionData;
import me.rockyhawk.commandpanels.openpanelsmanager.OpenGUI;
import me.rockyhawk.commandpanels.openpanelsmanager.OpenPanelsLoader;
import me.rockyhawk.commandpanels.openpanelsmanager.WorldPermissions;
import me.rockyhawk.commandpanels.openpanelsmanager.UtilsPanelsLoader;
import me.rockyhawk.commandpanels.openwithitem.HotbarItemLoader;
import me.rockyhawk.commandpanels.openwithitem.SwapItemEvent;
import me.rockyhawk.commandpanels.openwithitem.UtilsChestSortEvent;
import me.rockyhawk.commandpanels.openwithitem.UtilsOpenWithItem;
import me.rockyhawk.commandpanels.panelblocks.BlocksTabComplete;
import me.rockyhawk.commandpanels.panelblocks.BlocksCommand;
import me.rockyhawk.commandpanels.panelblocks.PanelBlockOnClick;
import me.rockyhawk.commandpanels.playerinventoryhandler.InventorySaver;
import me.rockyhawk.commandpanels.playerinventoryhandler.ItemStackSerializer;
import me.rockyhawk.commandpanels.playerinventoryhandler.pickupevent.EntityPickupEvent;
import me.rockyhawk.commandpanels.playerinventoryhandler.pickupevent.LegacyPlayerEvent;
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
    public LoadReloadCommand reloader;

    public CommandRunner commandRunner;
    public PanelDataLoader panelData;
    public PanelDataPlayerManager panelDataPlayers;

    public Placeholders placeholders;
    public DebugManager debug;
    public CreateText tex;
    public HexColours hex;

    public MiniMessageUtils miniMessage;

    public ExecuteOpenVoids openVoids;
    public ItemCreation itemCreate;
    public HasSections has;
    public GetCustomHeads customHeads;
    public Updater updater;
    public PlayerHeads getHeads;
    public ClassicPotionData classicPotion;
    public LegacyPotionData legacyPotion;
    public LegacyVersion legacy;

    public OpenPanelsLoader openPanels;
    public OpenGUI createGUI;
    public WorldPermissions worldPerms;
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
        panelData = new PanelDataLoader(this);
        inventorySaver = new InventorySaver(this);
        configHandler = new ConfigHandler(this);
        econ = null;

        deluxeConverter = new CompatibilityConverter(this);
        reloader = new LoadReloadCommand(this);

        commandRunner = new CommandRunner(this);
        panelDataPlayers = new PanelDataPlayerManager();

        placeholders = new Placeholders(this);
        debug = new DebugManager();
        tex = new CreateText(this);
        hex = new HexColours(this);

        miniMessage = null;

        openVoids = new ExecuteOpenVoids(this);
        itemCreate = new ItemCreation(this);
        has = new HasSections(this);
        customHeads = new GetCustomHeads(this);
        updater = new Updater(this);
        getHeads = new PlayerHeads(this);
        classicPotion = new ClassicPotionData();
        legacyPotion = new LegacyPotionData();
        legacy = new LegacyVersion(this);

        openPanels = new OpenPanelsLoader(this);
        createGUI = new OpenGUI(this);
        worldPerms = new WorldPermissions();
        hotbar = new HotbarItemLoader(this);
        nbt = new NBTManager(this);

        generator = new GenUtils(this);
        itemSerializer = new ItemStackSerializer(this);
        inputUtils = new PlayerInputUtils(this);

        tag = tex.colour(configHandler.config.getString("config.format.tag"));

        //setup class files
        setupEconomy();
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getCommand("commandpanel").setExecutor(new PanelCommand(this));
        plugin.getCommand("commandpanel").setTabCompleter(new PanelTabComplete(this));

        plugin.getCommand("commandpanelgenerate").setTabCompleter(new TabCompleteGenerate());
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

        if(this.legacy.MAJOR_VERSION.greaterThanOrEqualTo(MinecraftVersions.v1_12)){
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
            miniMessage = new MiniMessageUtils(this);
        } catch (ClassNotFoundException ignore) {
            //do not initialise miniMessage
        }

        Bukkit.getServer().getPluginManager().registerEvents(new Utils(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(inventorySaver, plugin);
        Bukkit.getServer().getPluginManager().registerEvents(inputUtils, plugin);
        Bukkit.getServer().getPluginManager().registerEvents(panelDataPlayers, plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new UtilsPanelsLoader(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(generator, plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new ItemFallManager(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new OpenOnJoin(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new OutsideClickEvent(this), plugin);
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
            Bukkit.getServer().getPluginManager().registerEvents(new PanelCommands(this), plugin);
        }
        if(configHandler.isTrue("config.hotbar-items")){
            Bukkit.getServer().getPluginManager().registerEvents(new UtilsOpenWithItem(this), plugin);
        }
        if(configHandler.isTrue("config.panel-blocks")){
            Objects.requireNonNull(plugin.getCommand("commandpanelblock")).setExecutor(new BlocksCommand(this));
            Objects.requireNonNull(plugin.getCommand("commandpanelblock")).setTabCompleter(new BlocksTabComplete(this));
            Bukkit.getServer().getPluginManager().registerEvents(new PanelBlockOnClick(this), plugin);
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
