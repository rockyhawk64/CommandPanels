package me.rockyhawk.commandpanels;

import io.lumine.mythic.lib.api.item.NBTItem;
import me.rockyhawk.commandpanels.api.CommandPanelsAPI;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.classresources.ExecuteOpenVoids;
import me.rockyhawk.commandpanels.classresources.GetCustomHeads;
import me.rockyhawk.commandpanels.classresources.HasSections;
import me.rockyhawk.commandpanels.classresources.ItemCreation;
import me.rockyhawk.commandpanels.classresources.placeholders.expansion.CpPlaceholderExpansion;
import me.rockyhawk.commandpanels.completetabs.DataTabComplete;
import me.rockyhawk.commandpanels.completetabs.ImportTabComplete;
import me.rockyhawk.commandpanels.classresources.item_fall.ItemFallManager;
import me.rockyhawk.commandpanels.classresources.placeholders.CreateText;
import me.rockyhawk.commandpanels.classresources.placeholders.HexColours;
import me.rockyhawk.commandpanels.classresources.placeholders.Placeholders;
import me.rockyhawk.commandpanels.commands.*;
import me.rockyhawk.commandpanels.commandtags.CommandTags;
import me.rockyhawk.commandpanels.completetabs.CpTabComplete;
import me.rockyhawk.commandpanels.customcommands.Commandpanelcustom;
import me.rockyhawk.commandpanels.datamanager.DebugManager;
import me.rockyhawk.commandpanels.datamanager.PanelDataLoader;
import me.rockyhawk.commandpanels.generatepanels.Commandpanelsgenerate;
import me.rockyhawk.commandpanels.generatepanels.GenUtils;
import me.rockyhawk.commandpanels.generatepanels.TabCompleteGenerate;
import me.rockyhawk.commandpanels.editor.CPEventHandler;
import me.rockyhawk.commandpanels.editor.CommandPanelsEditorCommand;
import me.rockyhawk.commandpanels.editor.CommandPanelsEditorMain;
import me.rockyhawk.commandpanels.editor.CommandPanelsEditorTabComplete;
import me.rockyhawk.commandpanels.interactives.input.UserInputUtils;
import me.rockyhawk.commandpanels.interactives.Commandpanelrefresher;
import me.rockyhawk.commandpanels.interactives.OpenOnJoin;
import me.rockyhawk.commandpanels.ioclasses.nbt.NBTManager;
import me.rockyhawk.commandpanels.ioclasses.legacy.LegacyVersion;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
import me.rockyhawk.commandpanels.ioclasses.legacy.PlayerHeads;
import me.rockyhawk.commandpanels.openpanelsmanager.*;
import me.rockyhawk.commandpanels.openwithitem.HotbarItemLoader;
import me.rockyhawk.commandpanels.openwithitem.SwapItemEvent;
import me.rockyhawk.commandpanels.openwithitem.UtilsChestSortEvent;
import me.rockyhawk.commandpanels.openwithitem.UtilsOpenWithItem;
import me.rockyhawk.commandpanels.panelblocks.BlocksTabComplete;
import me.rockyhawk.commandpanels.panelblocks.Commandpanelblocks;
import me.rockyhawk.commandpanels.panelblocks.PanelBlockOnClick;
import me.rockyhawk.commandpanels.playerinventoryhandler.InventorySaver;
import me.rockyhawk.commandpanels.playerinventoryhandler.ItemStackSerializer;
import me.rockyhawk.commandpanels.updater.Updater;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.Callable;

public class CommandPanels extends JavaPlugin{
    public YamlConfiguration config;
    public Economy econ = null;
    public boolean openWithItem = false; //this will be true if there is a panel with open-with-item

    //initialise the tag
    public String tag = "[CommandPanels]";

    public List<Player> generateMode = new ArrayList<>(); //players that are currently in generate mode
    public List<Panel> panelList = new ArrayList<>(); //contains all the panels that are included in the panels folder

    //get alternate classes
    public CommandPanelsEditorMain editorMain = new CommandPanelsEditorMain(this);

    public CommandTags commandTags = new CommandTags(this);
    public PanelDataLoader panelData = new PanelDataLoader(this);
    public Placeholders placeholders = new Placeholders(this);
    public DebugManager debug = new DebugManager(this);
    public CreateText tex = new CreateText(this);
    public HexColours hex = new HexColours(this);

    public ExecuteOpenVoids openVoids = new ExecuteOpenVoids(this);
    public ItemCreation itemCreate = new ItemCreation(this);
    public HasSections has = new HasSections(this);
    public GetCustomHeads customHeads = new GetCustomHeads(this);
    public Updater updater = new Updater(this);
    public PlayerHeads getHeads = new PlayerHeads(this);
    public LegacyVersion legacy = new LegacyVersion(this);

    public OpenPanelsLoader openPanels = new OpenPanelsLoader(this);
    public OpenGUI createGUI = new OpenGUI(this);
    public PanelPermissions panelPerms = new PanelPermissions(this);
    public HotbarItemLoader hotbar = new HotbarItemLoader(this);
    public NBTManager nbt = new NBTManager(this);

    public InventorySaver inventorySaver = new InventorySaver(this);
    public ItemStackSerializer itemSerializer = new ItemStackSerializer(this);
    public UserInputUtils inputUtils = new UserInputUtils(this);

    public File panelsf = new File(this.getDataFolder() + File.separator + "panels");
    public YamlConfiguration blockConfig; //where panel block locations are stored

    public void onEnable() {
        Bukkit.getLogger().info("[CommandPanels] RockyHawk's CommandPanels v" + this.getDescription().getVersion() + " Plugin Loading...");

        //register config files
        this.blockConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "blocks.yml"));
        panelData.dataConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "data.yml"));
        inventorySaver.inventoryConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "inventories.yml"));
        this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "config.yml"));

        //save the config.yml file
        File configFile = new File(this.getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists()) {
            //generate a new config file from internal resources
            try {
                FileConfiguration configFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("config.yml")));
                configFileConfiguration.save(configFile);
                this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "config.yml"));
            } catch (IOException var11) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Could not save the config file!");
            }
        }else{
            //check if the config file has any missing elements
            try {
                YamlConfiguration configFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("config.yml")));
                this.config.addDefaults(configFileConfiguration);
                this.config.options().copyDefaults(true);
                this.config.save(new File(this.getDataFolder() + File.separator + "config.yml"));
            } catch (IOException var10) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Could not save the config file!");
            }
        }

        //set version to latest version
        if (Objects.requireNonNull(this.config.getString("updater.update-checks")).equalsIgnoreCase("true")) {
            updater.githubNewUpdate(true);
        }

        //setup class files
        this.setupEconomy();
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        new Metrics(this);
        Objects.requireNonNull(this.getCommand("commandpanel")).setExecutor(new Commandpanel(this));
        Objects.requireNonNull(this.getCommand("commandpanel")).setTabCompleter(new CpTabComplete(this));

        Objects.requireNonNull(this.getCommand("commandpanelgenerate")).setTabCompleter(new TabCompleteGenerate(this));
        Objects.requireNonNull(this.getCommand("commandpanelgenerate")).setExecutor(new Commandpanelsgenerate(this));

        Objects.requireNonNull(this.getCommand("commandpaneldata")).setTabCompleter(new DataTabComplete(this));
        Objects.requireNonNull(this.getCommand("commandpaneldata")).setExecutor(new Commandpanelsdata(this));

        Objects.requireNonNull(this.getCommand("commandpanelimport")).setExecutor(new CommandPanelImport(this));
        Objects.requireNonNull(this.getCommand("commandpanelimport")).setTabCompleter(new ImportTabComplete(this));

        Objects.requireNonNull(this.getCommand("commandpanelreload")).setExecutor(new Commandpanelsreload(this));
        Objects.requireNonNull(this.getCommand("commandpaneldebug")).setExecutor(new Commandpanelsdebug(this));
        Objects.requireNonNull(this.getCommand("commandpanelversion")).setExecutor(new Commandpanelversion(this));
        Objects.requireNonNull(this.getCommand("commandpanellist")).setExecutor(new Commandpanelslist(this));
        this.getServer().getPluginManager().registerEvents(new Utils(this), this);
        this.getServer().getPluginManager().registerEvents(inventorySaver, this);
        this.getServer().getPluginManager().registerEvents(inputUtils, this);
        this.getServer().getPluginManager().registerEvents(new UtilsPanelsLoader(this), this);
        this.getServer().getPluginManager().registerEvents(new GenUtils(this), this);
        this.getServer().getPluginManager().registerEvents(new ItemFallManager(this), this);
        this.getServer().getPluginManager().registerEvents(new OpenOnJoin(this), this);

        //load in the updater if requested
        if (Objects.requireNonNull(config.getString("updater.update-checks")).equalsIgnoreCase("true")) {
            this.getServer().getPluginManager().registerEvents(updater, this);
        }

        //load in PlaceholderAPI Expansion
        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new CpPlaceholderExpansion(this).register();
        }

        //load in all built in command tags
        commandTags.registerBuiltInTags();

        //if refresh-panels set to false, don't load this
        if(Objects.requireNonNull(config.getString("config.refresh-panels")).equalsIgnoreCase("true")){
            this.getServer().getPluginManager().registerEvents(new Commandpanelrefresher(this), this);
        }

        //if custom-commands set to false, don't load this
        if(Objects.requireNonNull(config.getString("config.custom-commands")).equalsIgnoreCase("true")){
            this.getServer().getPluginManager().registerEvents(new Commandpanelcustom(this), this);
        }

        //if hotbar-items set to false, don't load this
        if(Objects.requireNonNull(config.getString("config.hotbar-items")).equalsIgnoreCase("true")){
            this.getServer().getPluginManager().registerEvents(new UtilsOpenWithItem(this), this);
        }

        //if ingame-editor set to false, don't load this
        if(Objects.requireNonNull(config.getString("config.ingame-editor")).equalsIgnoreCase("true")){
            this.getServer().getPluginManager().registerEvents(new CPEventHandler(this), this);
            Objects.requireNonNull(this.getCommand("commandpaneledit")).setTabCompleter(new CommandPanelsEditorTabComplete(this));
            Objects.requireNonNull(this.getCommand("commandpaneledit")).setExecutor(new CommandPanelsEditorCommand(this));
        }

        //if panel-blocks set to false, don't load this
        if(Objects.requireNonNull(config.getString("config.panel-blocks")).equalsIgnoreCase("true")){
            Objects.requireNonNull(this.getCommand("commandpanelblock")).setExecutor(new Commandpanelblocks(this));
            Objects.requireNonNull(this.getCommand("commandpanelblock")).setTabCompleter(new BlocksTabComplete(this));
            this.getServer().getPluginManager().registerEvents(new PanelBlockOnClick(this), this);
        }

        //if 1.8 don't use this
        if (!Bukkit.getVersion().contains("1.8")) {
            this.getServer().getPluginManager().registerEvents(new SwapItemEvent(this), this);
        }

        //if plugin ChestSort is enabled
        if(getServer().getPluginManager().isPluginEnabled("ChestSort")){
            this.getServer().getPluginManager().registerEvents(new UtilsChestSortEvent(this), this);
        }

        //save the example_top.yml file and the template.yml file
        if (!this.panelsf.exists()) {
            try {
                if(legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)){
                    FileConfiguration exampleFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("exampleLegacy.yml")));
                    exampleFileConfiguration.save(new File(this.panelsf + File.separator + "example.yml"));
                }else {
                    //top
                    FileConfiguration exampleFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("example_top.yml")));
                    exampleFileConfiguration.save(new File(this.panelsf + File.separator + "example_top.yml"));
                    //middle one
                    exampleFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("example_middle_one.yml")));
                    exampleFileConfiguration.save(new File(this.panelsf + File.separator + "example_middle_one.yml"));
                    //middle two
                    exampleFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("example_middle_two.yml")));
                    exampleFileConfiguration.save(new File(this.panelsf + File.separator + "example_middle_two.yml"));
                    //bottom
                    exampleFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("example_bottom.yml")));
                    exampleFileConfiguration.save(new File(this.panelsf + File.separator + "example_bottom.yml"));
                }
                FileConfiguration templateFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("template.yml")));
                templateFileConfiguration.save(new File(this.panelsf + File.separator + "template.yml"));
            } catch (IOException var11) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Could not save the example file!");
            }
        }

        //load panelFiles
        reloadPanelFiles();

        //do hotbar items
        hotbar.reloadHotbarSlots();

        //add custom charts bStats
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SingleLineChart("panels_amount", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                //this is the total panels loaded
                return panelList.size();
            }
        }));

        //get tag
        tag = tex.colour(config.getString("config.format.tag"));

        Bukkit.getLogger().info("[CommandPanels] RockyHawk's CommandPanels v" + this.getDescription().getVersion() + " Plugin Loaded!");
    }

    public void onDisable() {
        //close all the panels
        for(String name : openPanels.openPanels.keySet()){
            openPanels.closePanelForLoader(name, PanelPosition.Top);
            try {
                Bukkit.getPlayer(name).closeInventory();
            }catch (Exception ignore){}
        }

        //save files
        panelData.saveDataFile();
        inventorySaver.saveInventoryFile();
        updater.autoUpdatePlugin(this.getFile().getName());
        Bukkit.getLogger().info("RockyHawk's CommandPanels Plugin Disabled, aww man.");
    }

    public static CommandPanelsAPI getAPI(){
        return new CommandPanelsAPI(JavaPlugin.getPlugin(CommandPanels.class));
    }

    public ItemStack setName(Panel panel, ItemStack renamed, String customName, List<String> lore, Player p, Boolean usePlaceholders, Boolean useColours, Boolean hideAttributes) {
        try {
            ItemMeta renamedMeta = renamed.getItemMeta();
            //set cp placeholders
            if(usePlaceholders){
                customName = tex.placeholdersNoColour(panel,PanelPosition.Top,p,customName);
            }
            if(useColours){
                customName = tex.colour(customName);
            }

            assert renamedMeta != null;
            //hiding attributes will add an NBT tag
            if(hideAttributes) {
                renamedMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                renamedMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                renamedMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                //HIDE_DYE was added into 1.17 api
                if(legacy.LOCAL_VERSION.greaterThanOrEqualTo(MinecraftVersions.v1_17)){
                    renamedMeta.addItemFlags(ItemFlag.HIDE_DYE);
                }
            }
            if (customName != null) {
                renamedMeta.setDisplayName(customName);
            }

            List<String> re_lore;
            if (lore != null) {
                if(usePlaceholders && useColours){
                    re_lore = tex.placeholdersList(panel,PanelPosition.Top, p, lore, true);
                }else if(usePlaceholders){
                    re_lore = tex.placeholdersNoColour(panel,PanelPosition.Top,p, lore);
                }else if(useColours){
                    re_lore = tex.placeholdersList(panel,PanelPosition.Top, p, lore, false);
                }else{
                    re_lore = lore;
                }
                renamedMeta.setLore(splitListWithEscape(re_lore));
            }
            renamed.setItemMeta(renamedMeta);
        } catch (Exception ignored) {}
        return renamed;
    }

    private void setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
        } else {
            RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
            } else {
                this.econ = (Economy) rsp.getProvider();
            }
        }
    }

    public boolean checkPanels(YamlConfiguration temp) {
        try {
            return temp.contains("panels");
        } catch (Exception var3) {
            return false;
        }
    }

    //check for duplicate panel names
    public boolean checkDuplicatePanel(CommandSender sender){
        List<String> apanels = new ArrayList<>();
        for(Panel panel : panelList){
            apanels.add(panel.getName());
        }

        //names is a list of the titles for the Panels
        Set<String> oset = new HashSet<String>(apanels);
        if (oset.size() < apanels.size()) {
            //there are duplicate panel names
            ArrayList<String> opanelsTemp = new ArrayList<String>();
            for(String tempName : apanels){
                if(opanelsTemp.contains(tempName)){
                    sender.sendMessage(tex.colour(tag) + ChatColor.RED + " Error duplicate panel name: " + tempName);
                    return false;
                }
                opanelsTemp.add(tempName);
            }
            return false;
        }
        return true;
    }

    //look through all files in all folders
    public void fileNamesFromDirectory(File directory) {
        for (String fileName : Objects.requireNonNull(directory.list())) {
            if(new File(directory + File.separator + fileName).isDirectory()){
                fileNamesFromDirectory(new File(directory + File.separator + fileName));
                continue;
            }

            try {
                int ind = fileName.lastIndexOf(".");
                if (!fileName.substring(ind).equalsIgnoreCase(".yml") && !fileName.substring(ind).equalsIgnoreCase(".yaml")) {
                    continue;
                }
            }catch (Exception ex){
                continue;
            }

            //check before adding the file to commandpanels
            if(!checkPanels(YamlConfiguration.loadConfiguration(new File(directory + File.separator + fileName)))){
                this.getServer().getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Error in: " + fileName);
                continue;
            }
            for (String tempName : Objects.requireNonNull(YamlConfiguration.loadConfiguration(new File(directory + File.separator + fileName)).getConfigurationSection("panels")).getKeys(false)) {
                panelList.add(new Panel(new File((directory + File.separator + fileName)),tempName));
                if(YamlConfiguration.loadConfiguration(new File(directory + File.separator + fileName)).contains("panels." + tempName + ".open-with-item")) {
                    openWithItem = true;
                }
            }
        }
    }

    public void reloadPanelFiles() {
        panelList.clear();
        openWithItem = false;
        //load panel files
        fileNamesFromDirectory(panelsf);
    }

    public void debug(Exception e, Player p) {
        if (p == null) {
            if(debug.consoleDebug){
                getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "[CommandPanels] The plugin has generated a debug error, find the error below");
                e.printStackTrace();
            }
        }else{
            if(debug.isEnabled(p)){
                p.sendMessage(tag + ChatColor.DARK_RED + "Check the console for a detailed error.");
                getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "[CommandPanels] The plugin has generated a debug error, find the error below");
                e.printStackTrace();
            }
        }
    }

    public void helpMessage(CommandSender p) {
        p.sendMessage(tex.colour( tag + ChatColor.GREEN + "Commands:"));
        p.sendMessage(ChatColor.GOLD + "/cp <panel> [player:item] [player] " + ChatColor.WHITE + "Open a command panel.");
        if (p.hasPermission("commandpanel.reload")) {
            p.sendMessage(ChatColor.GOLD + "/cpr " + ChatColor.WHITE + "Reloads plugin config.");
        }
        if (p.hasPermission("commandpanel.generate")) {
            p.sendMessage(ChatColor.GOLD + "/cpg <rows> " + ChatColor.WHITE + "Generate GUI from popup menu.");
        }
        if (p.hasPermission("commandpanel.version")) {
            p.sendMessage(ChatColor.GOLD + "/cpv " + ChatColor.WHITE + "Display the current version.");
        }
        if (p.hasPermission("commandpanel.update")) {
            p.sendMessage(ChatColor.GOLD + "/cpv latest " + ChatColor.WHITE + "Download the latest update upon server reload/restart.");
            p.sendMessage(ChatColor.GOLD + "/cpv [version:cancel] " + ChatColor.WHITE + "Download an update upon server reload/restart.");
        }
        if (p.hasPermission("commandpanel.import")) {
            p.sendMessage(ChatColor.GOLD + "/cpi [file name] [URL] " + ChatColor.WHITE + "Downloads a panel from a raw link online.");
        }
        if (p.hasPermission("commandpanel.list")) {
            p.sendMessage(ChatColor.GOLD + "/cpl " + ChatColor.WHITE + "Lists the currently loaded panels.");
        }
        if (p.hasPermission("commandpanel.data")) {
            p.sendMessage(ChatColor.GOLD + "/cpdata " + ChatColor.WHITE + "Change panel data for a user.");
        }
        if (p.hasPermission("commandpanel.debug")) {
            p.sendMessage(ChatColor.GOLD + "/cpd " + ChatColor.WHITE + "Enable and Disable debug mode globally.");
        }
        if (p.hasPermission("commandpanel.block.add")) {
            p.sendMessage(ChatColor.GOLD + "/cpb add <panel> " + ChatColor.WHITE + "Add panel to a block being looked at.");
        }
        if (p.hasPermission("commandpanel.block.remove")) {
            p.sendMessage(ChatColor.GOLD + "/cpb remove " + ChatColor.WHITE + "Removes any panel assigned to a block looked at.");
        }
        if (p.hasPermission("commandpanel.block.list")) {
            p.sendMessage(ChatColor.GOLD + "/cpb list " + ChatColor.WHITE + "List blocks that will open panels.");
        }
        if (p.hasPermission("commandpanel.edit")) {
            p.sendMessage(ChatColor.GOLD + "/cpe <panel> " + ChatColor.WHITE + "Edit a panel with the Panel Editor.");
        }
    }

    public final Map<String, Color> colourCodes = new HashMap<String, Color>() {{
        put("AQUA", Color.AQUA);
        put("BLUE", Color.BLUE);
        put("GRAY", Color.GRAY);
        put("GREEN", Color.GREEN);
        put("RED", Color.RED);
        put("WHITE", Color.WHITE);
        put("BLACK", Color.BLACK);
        put("FUCHSIA", Color.FUCHSIA);
        put("LIME", Color.LIME);
        put("MAROON", Color.MAROON);
        put("NAVY", Color.NAVY);
        put("OLIVE", Color.OLIVE);
        put("ORANGE", Color.ORANGE);
        put("PURPLE", Color.PURPLE);
        put("SILVER", Color.SILVER);
        put("TEAL", Color.TEAL);
        put("YELLOW", Color.YELLOW);
    }};

    public Reader getReaderFromStream(InputStream initialStream) throws IOException {
        //this reads the encrypted resource files in the jar file
        byte[] buffer = IOUtils.toByteArray(initialStream);
        return new CharSequenceReader(new String(buffer));
    }

    //split lists using \n escape character
    public List<String> splitListWithEscape(List<String> list){
        List<String> output = new ArrayList<>();
        for(String str : list){
            output.addAll(Arrays.asList(str.split("\\\\n")));
        }
        return output;
    }

    public int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    //returns true if the item is the MMO Item
    public boolean isMMOItem(ItemStack itm, String type, String id){
        try {
            if (getServer().getPluginManager().isPluginEnabled("MMOItems")) {
                NBTItem nbt = NBTItem.get(itm);
                if (nbt.getType().equalsIgnoreCase(type) && nbt.getString("MMOITEMS_ITEM_ID").equalsIgnoreCase(id)){
                    return true;
                }
                itm.getType();
            }
        }catch (Exception ex){
            debug(ex,null);
        }
        return false;
    }
}
