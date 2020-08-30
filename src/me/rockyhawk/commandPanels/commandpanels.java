package me.rockyhawk.commandPanels;

import com.Ben12345rocks.VotingPlugin.UserManager.UserManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.clip.placeholderapi.PlaceholderAPI;
import me.realized.tokenmanager.api.TokenManager;
import me.rockyhawk.commandPanels.ClassResources.*;
import me.rockyhawk.commandPanels.commands.*;
import me.rockyhawk.commandPanels.completeTabs.cpTabComplete;
import me.rockyhawk.commandPanels.generatePanels.commandpanelsgenerate;
import me.rockyhawk.commandPanels.generatePanels.newGenUtils;
import me.rockyhawk.commandPanels.generatePanels.tabCompleteGenerate;
import me.rockyhawk.commandPanels.ingameEditor.cpIngameEditCommand;
import me.rockyhawk.commandPanels.ingameEditor.cpTabCompleteIngame;
import me.rockyhawk.commandPanels.ingameEditor.editorUserInput;
import me.rockyhawk.commandPanels.ingameEditor.editorUtils;

import me.rockyhawk.commandPanels.ioClasses.sequence_1_13;
import me.rockyhawk.commandPanels.ioClasses.sequence_1_14;

import me.rockyhawk.commandPanels.openWithItem.utilsOpenWithItem;
import me.rockyhawk.commandPanels.panelBlocks.blocksTabComplete;
import me.rockyhawk.commandPanels.panelBlocks.commandpanelblocks;
import me.rockyhawk.commandPanels.panelBlocks.panelBlockOnClick;
import me.rockyhawk.commandPanels.premium.commandpanelUserInput;
import me.rockyhawk.commandPanels.premium.commandpanelrefresher;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class commandpanels extends JavaPlugin {
    public YamlConfiguration config;
    public Economy econ = null;
    public boolean update = false;
    public boolean debug = false;
    public boolean openWithItem = false; //this will be true if there is a panel with open-with-item

    public List<Player> generateMode = new ArrayList<>(); //players that are currently in generate mode
    public List<String> panelRunning = new ArrayList<>();
    public List<String[]> userInputStrings = new ArrayList<>();
    public List<String[]> editorInputStrings = new ArrayList<>();
    public List<String> panelFiles = new ArrayList<>(); //names of all the files in the panels folder including extension
    public List<String[]> panelNames = new ArrayList<>(); //this will return something like {"mainMenuPanel","4"} which means the 4 is for panelFiles.get(4). So you know which file it is for

    //get alternate classes
    public CommandTags commandTags = new CommandTags(this);
    public OpenEditorGuis editorGuis = new OpenEditorGuis(this);
    public ExecuteOpenVoids openVoids = new ExecuteOpenVoids(this);
    public ItemCreation itemCreate = new ItemCreation(this);
    public GetCustomHeads customHeads = new GetCustomHeads(this);

    public File panelsf;
    public YamlConfiguration blockConfig; //where panel block locations are stored

    public commandpanels() {
        this.panelsf = new File(this.getDataFolder() + File.separator + "panels");
        this.blockConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "blocks.yml"));
    }

    public void onEnable() {
        this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "config.yml"));
        Bukkit.getLogger().info("[CommandPanels] RockyHawk's CommandPanels v" + this.getDescription().getVersion() + " Plugin Loading...");
        this.setupEconomy();
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        new Metrics(this);
        Objects.requireNonNull(this.getCommand("commandpanel")).setExecutor(new commandpanel(this));
        Objects.requireNonNull(this.getCommand("commandpanel")).setTabCompleter(new cpTabComplete(this));
        Objects.requireNonNull(this.getCommand("commandpanelblock")).setTabCompleter(new blocksTabComplete(this));
        Objects.requireNonNull(this.getCommand("commandpanelgenerate")).setTabCompleter(new tabCompleteGenerate(this));
        Objects.requireNonNull(this.getCommand("commandpaneledit")).setTabCompleter(new cpTabCompleteIngame(this));
        Objects.requireNonNull(this.getCommand("commandpanelgenerate")).setExecutor(new commandpanelsgenerate(this));
        Objects.requireNonNull(this.getCommand("commandpanelreload")).setExecutor(new commandpanelsreload(this));
        Objects.requireNonNull(this.getCommand("commandpaneldebug")).setExecutor(new commandpanelsdebug(this));
        Objects.requireNonNull(this.getCommand("commandpanelclose")).setExecutor(new commandpanelclose(this));
        Objects.requireNonNull(this.getCommand("commandpanelversion")).setExecutor(new commandpanelversion(this));
        Objects.requireNonNull(this.getCommand("commandpaneladdons")).setExecutor(new commandpanelresources(this));
        Objects.requireNonNull(this.getCommand("commandpanellist")).setExecutor(new commandpanelslist(this));
        Objects.requireNonNull(this.getCommand("commandpaneledit")).setExecutor(new cpIngameEditCommand(this));
        Objects.requireNonNull(this.getCommand("commandpanelblock")).setExecutor(new commandpanelblocks(this));
        this.getServer().getPluginManager().registerEvents(new utils(this), this);
        this.getServer().getPluginManager().registerEvents(new utilsOpenWithItem(this), this);
        this.getServer().getPluginManager().registerEvents(new editorUtils(this), this);
        this.getServer().getPluginManager().registerEvents(new newGenUtils(this), this);
        this.getServer().getPluginManager().registerEvents(new commandpanelcustom(this), this);
        this.getServer().getPluginManager().registerEvents(new commandpanelUserInput(this), this);
        this.getServer().getPluginManager().registerEvents(new editorUserInput(this), this);
        this.getServer().getPluginManager().registerEvents(new commandpanelrefresher(this), this);
        this.getServer().getPluginManager().registerEvents(new panelBlockOnClick(this), this);

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

        //save the example.yml file
        if (!this.panelsf.exists() || Objects.requireNonNull(this.panelsf.list()).length == 0) {
            try {
                FileConfiguration exampleFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("example.yml")));
                exampleFileConfiguration.save(new File(this.panelsf + File.separator + "example.yml"));
            } catch (IOException var11) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Could not save the example file!");
            }
        }

        if (Objects.requireNonNull(this.config.getString("config.update-notifications")).equalsIgnoreCase("true")) {
            githubNewUpdate(true);
        }

        //load panelFiles
        reloadPanelFiles();

        Bukkit.getLogger().info("[CommandPanels] RockyHawk's CommandPanels v" + this.getDescription().getVersion() + " Plugin Loaded!");
    }

    public void onDisable() {
        Bukkit.getLogger().info("RockyHawk's CommandPanels Plugin Disabled, aww man.");
    }

    public Inventory openGui(String panels, Player p, YamlConfiguration pconfig, int onOpen, int animateValue) {
        String tag = this.config.getString("config.format.tag") + " ";
        if (Integer.parseInt(Objects.requireNonNull(pconfig.getString("panels." + panels + ".rows"))) < 7 && Integer.parseInt(Objects.requireNonNull(pconfig.getString("panels." + panels + ".rows"))) > 0) {
            Inventory i;
            if (onOpen != 3) {
                //use the regular inventory
                i = Bukkit.createInventory(null, Integer.parseInt(Objects.requireNonNull(pconfig.getString("panels." + panels + ".rows"))) * 9, papi( Objects.requireNonNull(pconfig.getString("panels." + panels + ".title"))));
            } else {
                //this means it is the Editor window
                i = Bukkit.createInventory(null, Integer.parseInt(Objects.requireNonNull(pconfig.getString("panels." + panels + ".rows"))) * 9, papi( ChatColor.GRAY + "Editing Panel: " + pconfig.getString("panels." + panels + ".title")));
            }
            String item = "";

            String key;
            for (Iterator var6 = Objects.requireNonNull(pconfig.getConfigurationSection("panels." + panels + ".item")).getKeys(false).iterator(); var6.hasNext(); item = item + key + " ") {
                key = (String) var6.next();
            }

            item = item.trim();
            int c;
            for (c = 0; item.split("\\s").length - 1 >= c; ++c) {
                if(item.equals("")){
                    //skip putting any items in the inventory if it is empty
                    break;
                }
                String section = "";
                //onOpen needs to not be 3 so the editor won't include hasperm and hasvalue, etc items
                if (onOpen != 3) {
                    section = itemCreate.hasSection(pconfig.getConfigurationSection("panels." + panels + ".item." + Integer.parseInt(item.split("\\s")[c])), p);
                    //This section is for animations below here: VISUAL ONLY

                    //check for if there is animations inside the items section
                    if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + section + ".animate" + animateValue)) {
                        //check for if it contains the animate that has the animvatevalue
                        if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + section + ".animate" + animateValue)) {
                            section = section + ".animate" + animateValue;
                        }
                    }
                }
                ItemStack s = itemCreate.makeItemFromConfig(Objects.requireNonNull(pconfig.getConfigurationSection("panels." + panels + ".item." + item.split("\\s")[c] + section)), p, onOpen != 3, onOpen != 3);
                try {
                    i.setItem(Integer.parseInt(item.split("\\s")[c]), s);
                } catch (ArrayIndexOutOfBoundsException var24) {
                    debug(var24);
                    if (debug) {
                        p.sendMessage(papi(tag + this.config.getString("config.format.error") + " item: One of the items does not fit in the Panel!"));
                    }
                }
            }
            if (pconfig.contains("panels." + panels + ".empty") && !Objects.equals(pconfig.getString("panels." + panels + ".empty"), "AIR")) {
                for (c = 0; Integer.parseInt(Objects.requireNonNull(pconfig.getString("panels." + panels + ".rows"))) * 9 - 1 >= c; ++c) {
                    boolean found = false;
                    if(!item.equals("")) {
                        for (int f = 0; item.split("\\s").length - 1 >= f; ++f) {
                            if (Integer.parseInt(item.split("\\s")[f]) == c) {
                                found = true;
                            }
                        }
                    }
                    if (!found) {
                        ItemStack empty;
                        try {
                            empty = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(pconfig.getString("panels." + panels + ".empty")).toUpperCase())), 1);
                            if (empty.getType() == Material.AIR) {
                                continue;
                            }
                        } catch (IllegalArgumentException | NullPointerException var26) {
                            debug(var26);
                            p.sendMessage(papi(tag + this.config.getString("config.format.error") + " empty: " + pconfig.getString("panels." + panels + ".empty")));
                            return null;
                        }

                        ItemMeta renamedMeta = empty.getItemMeta();
                        assert renamedMeta != null;
                        renamedMeta.setDisplayName(" ");
                        empty.setItemMeta(renamedMeta);
                        if (onOpen != 3) {
                            //only place empty items if not editing
                            i.setItem(c, empty);
                        }
                    }
                }
            }
            if (papi( Objects.requireNonNull(pconfig.getString("panels." + panels + ".title"))).equals("Chest")) {
                p.sendMessage(papi(tag + this.config.getString("config.format.error") + " Title: Cannot be named Chest"));
                return null;
            }
            if (papi( Objects.requireNonNull(pconfig.getString("panels." + panels + ".title"))).contains("Editing Panel:")) {
                p.sendMessage(papi(tag + this.config.getString("config.format.error") + " Title: Cannot contain Editing Panel:"));
                return null;
            }
            if (papi( Objects.requireNonNull(pconfig.getString("panels." + panels + ".title"))).contains("Panel Settings:")) {
                p.sendMessage(papi(tag + this.config.getString("config.format.error") + " Title: Cannot contain Panel Settings:"));
                return null;
            }
            if (papi( Objects.requireNonNull(pconfig.getString("panels." + panels + ".title"))).contains("Item Settings:")) {
                p.sendMessage(papi(tag + this.config.getString("config.format.error") + " Title: Cannot contain Item Settings:"));
                return null;
            }
            if (papi( Objects.requireNonNull(pconfig.getString("panels." + panels + ".title"))).equals("Command Panels Editor")) {
                p.sendMessage(papi(tag + this.config.getString("config.format.error") + " Title: Cannot be named Command Panels Editor"));
                return null;
            }
            if (onOpen == 1 || onOpen == 3) {
                //onOpen 1 is default and 3 is for the editor
                p.openInventory(i);
            } else if (onOpen == 0) {
                //onOpen 0 will just refresh the panel
                p.getOpenInventory().getTopInventory().setStorageContents(i.getStorageContents());
            } else if (onOpen == 2) {
                //will return the inventory, not opening it at all
                return i;
            }
            return i;
        } else {
            p.sendMessage(papi(tag + this.config.getString("config.format.error") + " rows: " + pconfig.getString("panels." + panels + ".rows")));
            return null;
        }
    }

    public void setName(ItemStack renamed, String customName, List<String> lore, Player p, Boolean usePlaceholders, Boolean useColours) {
        try {
            ItemMeta renamedMeta = renamed.getItemMeta();
            //set cp placeholders
            if(usePlaceholders){
                customName = papiNoColour(p,customName);
            }
            if(useColours){
                customName = papi(customName);
            }

            assert renamedMeta != null;
            renamedMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            if (customName != null) {
                renamedMeta.setDisplayName(customName);
            }

            List<String> clore;
            if (lore != null) {
                if(usePlaceholders && useColours){
                    clore = papi(p, lore, true);
                }else if(usePlaceholders){
                    clore = papiNoColour(p, lore);
                }else if(useColours){
                    clore = papi(p, lore, false);
                }else{
                    clore = lore;
                }
                renamedMeta.setLore(clore);
            }
            renamed.setItemMeta(renamedMeta);
        } catch (Exception ignored) {
        }

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

    public String getHeadBase64(ItemStack var0) {
        if (var0.getType().equals(Material.PLAYER_HEAD) && var0.hasItemMeta()) {
            try {
                SkullMeta var1 = (SkullMeta) var0.getItemMeta();
                assert var1 != null;
                if (!var1.hasOwner()) {
                    Field var2 = var1.getClass().getDeclaredField("profile");
                    var2.setAccessible(true);
                    GameProfile var3 = (GameProfile) var2.get(var1);
                    Iterator var4 = var3.getProperties().get("textures").iterator();
                    if (var4.hasNext()) {
                        Property var5 = (Property) var4.next();
                        return var5.getValue();
                    }
                }
            }catch(Exception exc){/*If there is a problem with the head skip and return null*/}
        }
        return null;
    }

    //regular string papi
    public String papi(Player p, String setpapi) {
        try {
            setpapi = setCpPlaceholders(p,setpapi);
            if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                OfflinePlayer offp = getServer().getOfflinePlayer(p.getUniqueId());
                setpapi = PlaceholderAPI.setPlaceholders(offp, setpapi);
            }
            setpapi = translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', setpapi));
            return setpapi;
        }catch(NullPointerException e){
            return setpapi;
        }
    }

    //string papi with no colours
    public String papiNoColour(Player p, String setpapi) {
        try {
            setpapi = setCpPlaceholders(p,setpapi);
            if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                OfflinePlayer offp = getServer().getOfflinePlayer(p.getUniqueId());
                setpapi = PlaceholderAPI.setPlaceholders(offp, setpapi);
            }
            return setpapi;
        }catch(NullPointerException e){
            return setpapi;
        }
    }

    //regular string papi, but only colours so Player doesn't need to be there
    public String papi(String setpapi) {
        try {
            setpapi = translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', setpapi));
            return setpapi;
        }catch(NullPointerException e){
            return setpapi;
        }
    }

    //papi except if it is a String List
    public List<String> papi(Player p, List<String> setpapi, boolean placeholders) {
        try {
            if(placeholders) {
                int tempInt = 0;
                for (String temp : setpapi) {
                    setpapi.set(tempInt, setCpPlaceholders(p, temp));
                    tempInt += 1;
                }
                if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    OfflinePlayer offp = getServer().getOfflinePlayer(p.getUniqueId());
                    setpapi = PlaceholderAPI.setPlaceholders(offp, setpapi);
                }
            }
        }catch(Exception ignore){
            //this will be ignored as it is probably a null
            return null;
        }
        int tempInt = 0;
        //change colour
        for(String temp : setpapi){
            try {
                setpapi.set(tempInt, translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', temp)));
            }catch(NullPointerException ignore){
            }
            tempInt += 1;
        }
        return setpapi;
    }

    //papi except if it is a String List
    public List<String> papiNoColour(Player p, List<String> setpapi) {
        try {
            int tempInt = 0;
            for (String temp : setpapi) {
                setpapi.set(tempInt, setCpPlaceholders(p, temp));
                tempInt += 1;
            }
            if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                OfflinePlayer offp = getServer().getOfflinePlayer(p.getUniqueId());
                setpapi = PlaceholderAPI.setPlaceholders(offp, setpapi);
            }
        }catch(Exception ignore){
            //this will be ignored as it is probably a null
            return null;
        }
        return setpapi;
    }

    public String setCpPlaceholders(Player p, String str) {
        String tag = config.getString("config.format.tag") + " ";
        //replace nodes with PlaceHolders
        str = str.replaceAll("%cp-player-displayname%", p.getDisplayName());
        str = str.replaceAll("%cp-player-name%", p.getName());
        str = str.replaceAll("%cp-player-world%", p.getWorld().getName());
        str = str.replaceAll("%cp-player-x%", String.valueOf(Math.round(p.getLocation().getX())));
        str = str.replaceAll("%cp-player-y%", String.valueOf(Math.round(p.getLocation().getY())));
        str = str.replaceAll("%cp-player-z%", String.valueOf(Math.round(p.getLocation().getZ())));
        str = str.replaceAll("%cp-online-players%", Integer.toString(Bukkit.getServer().getOnlinePlayers().size()));
        //placeholder to check for server availability
        while (str.contains("%cp-server-")) {
            int start = str.indexOf("%cp-server-");
            int end = str.indexOf("%", str.indexOf("%cp-server-")+1);
            String ip_port = str.substring(start, end).replace("%cp-server-", "").replace("%","");
            Socket s = new Socket();
            try {
                s.connect(new InetSocketAddress(ip_port.split(":")[0], Integer.parseInt(ip_port.split(":")[1])), config.getInt("config.server-ping-timeout"));
                str = str.replace(str.substring(start, end) + "%", papi(p, "true"));
                s.close();
            }catch (IOException ex){
                str = str.replace(str.substring(start, end) + "%", papi(p, "false"));
            }
        }
        while (str.contains("%cp-player-online-")) {
            int start = str.indexOf("%cp-player-online-");
            int end = str.indexOf("-find%",str.indexOf("%cp-player-online-")+1);
            String playerLocation = str.substring(start, end).replace("%cp-player-online-", "");
            Player[] playerFind = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
            if (Integer.parseInt(playerLocation) > playerFind.length) {
                str = str.replace(str.substring(start, end) + "-find%", papi(p,Objects.requireNonNull(config.getString("config.format.offline"))));
            } else {
                str = str.replace(str.substring(start, end) + "-find%", playerFind[Integer.parseInt(playerLocation) - 1].getName());
            }
        }
        try {
            if (econ != null) {
                str = str.replaceAll("%cp-player-balance%", String.valueOf(Math.round(econ.getBalance(p))));
            }
        } catch (Exception place) {
            //skip
        }
        if (this.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
            TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
            assert api != null;
            str = str.replaceAll("%cp-tokenmanager-balance%", Long.toString(api.getTokens(p).orElse(0)));
        }
        if (this.getServer().getPluginManager().isPluginEnabled("VotingPlugin")) {
            str = str.replaceAll("%cp-votingplugin-points%", String.valueOf(UserManager.getInstance().getVotingPluginUser(p).getPoints()));
        }
        if (str.contains("%cp-player-input%")) {
            for (String[] key : userInputStrings) {
                if (key[0].equals(p.getName())) {
                    userInputStrings.add(new String[]{p.getName(), str});
                    return "commandpanels:commandpanelclose";
                }
            }
            userInputStrings.add(new String[]{p.getName(), str});
            List<String> inputMessages = new ArrayList<String>(config.getStringList("config.input-message"));
            for (String temp : inputMessages) {
                temp = temp.replaceAll("%cp-args%", Objects.requireNonNull(config.getString("config.input-cancel")));
                temp = temp.replaceAll("%cp-tag%", tag);
                p.sendMessage(papi(p, temp));
            }
            str = "commandpanels:commandpanelclose";
        }
        //end nodes with PlaceHolders
        return str;
    }

    //look through all files in all folders
    public void fileNamesFromDirectory(File directory) {
        for (String fileName : Objects.requireNonNull(directory.list())) {
            if(new File(directory + File.separator + fileName).isDirectory()){
                fileNamesFromDirectory(new File(directory + File.separator + fileName));
                continue;
            }
            int ind = fileName.lastIndexOf(".");
            if(!fileName.substring(ind).equalsIgnoreCase(".yml") && !fileName.substring(ind).equalsIgnoreCase(".yaml")){
                continue;
            }
            //check before adding the file to commandpanels
            if(!checkPanels(YamlConfiguration.loadConfiguration(new File(directory + File.separator + fileName)))){
                this.getServer().getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Error in: " + fileName);
                continue;
            }
            panelFiles.add((directory + File.separator + fileName).replace(panelsf.toString() + File.separator,""));
            for (String tempName : Objects.requireNonNull(YamlConfiguration.loadConfiguration(new File(directory + File.separator + fileName)).getConfigurationSection("panels")).getKeys(false)) {
                panelNames.add(new String[]{tempName, Integer.toString(panelFiles.size()-1)});
                if(YamlConfiguration.loadConfiguration(new File(directory + File.separator + fileName)).contains("panels." + tempName + ".open-with-item")) {
                    openWithItem = true;
                }
            }
        }
    }

    public void reloadPanelFiles() {
        panelFiles.clear();
        panelNames.clear();
        openWithItem = false;
        //load panel files
        fileNamesFromDirectory(panelsf);
    }

    public void debug(Exception e) {
        if (debug) {
            e.printStackTrace();
        }
    }

    public void helpMessage(CommandSender p) {
        String tag = config.getString("config.format.tag") + " ";
        p.sendMessage(papi( tag + ChatColor.GREEN + "Commands:"));
        p.sendMessage(ChatColor.GOLD + "/cp <panel> [player:item] [player] " + ChatColor.WHITE + "Open a command panel.");
        if (p.hasPermission("commandpanel.reload")) {
            p.sendMessage(ChatColor.GOLD + "/cpr " + ChatColor.WHITE + "Reloads plugin config.");
        }
        if (p.hasPermission("commandpanel.addons")) {
            p.sendMessage(ChatColor.GOLD + "/cpa " + ChatColor.WHITE + "View downloadable addons for CommandPanels.");
        }
        p.sendMessage(ChatColor.GOLD + "/cpc " + ChatColor.WHITE + "Close current GUI.");
        if (p.hasPermission("commandpanel.generate")) {
            p.sendMessage(ChatColor.GOLD + "/cpg <rows> " + ChatColor.WHITE + "Generate GUI from popup menu.");
        }
        if (p.hasPermission("commandpanel.version")) {
            p.sendMessage(ChatColor.GOLD + "/cpv " + ChatColor.WHITE + "Display the current version.");
        }
        if (p.hasPermission("commandpanel.edit")) {
            p.sendMessage(ChatColor.GOLD + "/cpe [panel] " + ChatColor.WHITE + "Edit a panel with the Panel Editor.");
        }
        if (p.hasPermission("commandpanel.list")) {
            p.sendMessage(ChatColor.GOLD + "/cpl " + ChatColor.WHITE + "Lists the currently loaded panels.");
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
        if(Bukkit.getVersion().contains("1.13")){
            return new sequence_1_13().getReaderFromStream(initialStream);
        }else{
            return new sequence_1_14().getReaderFromStream(initialStream);
        }
    }

    public String githubNewUpdate(boolean sendMessages){
        HttpURLConnection connection;
        String gitVersion;
        if(this.getDescription().getVersion().contains("-")){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GREEN + " Running a custom version.");
            }
            return null;
        }
        try{
            connection = (HttpURLConnection) new URL("https://raw.githubusercontent.com/rockyhawk64/CommandPanels/master/resource/plugin.yml").openConnection();
            connection.connect();
            gitVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine().split("\\s")[1];
            if(gitVersion.contains("-")){
                if(sendMessages) {
                    Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Cannot check for update.");
                }
                return null;
            }
            if(!this.getDescription().getVersion().equals(gitVersion)){
                if(sendMessages) {
                    Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " ================================================");
                    Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " An update for CommandPanels is available.");
                    Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " Download version " + gitVersion + " here:");
                    Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " https://www.spigotmc.org/resources/command-panels-custom-guis.67788/");
                    Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " ================================================");
                }
                return gitVersion;
            }
        }catch(IOException e){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Error checking for updates online.");
            }
            debug(e);
        }
        return null;
    }

    //used to translate hex colours into ChatColors
    public String translateHexColorCodes(String message) {
        final Pattern hexPattern = Pattern.compile("#" + "([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }
}
