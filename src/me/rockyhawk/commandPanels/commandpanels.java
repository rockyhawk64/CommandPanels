package me.rockyhawk.commandPanels;

import com.Ben12345rocks.VotingPlugin.UserManager.UserManager;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.realized.tokenmanager.api.TokenManager;
import me.rockyhawk.commandPanels.commands.*;
import me.rockyhawk.commandPanels.completeTabs.cpTabComplete;
import me.rockyhawk.commandPanels.generatePanels.commandpanelsgenerate;
import me.rockyhawk.commandPanels.generatePanels.newGenUtils;
import me.rockyhawk.commandPanels.generatePanels.tabCompleteGenerate;
import me.rockyhawk.commandPanels.ingameEditor.cpIngameEditCommand;
import me.rockyhawk.commandPanels.ingameEditor.cpTabCompleteIngame;
import me.rockyhawk.commandPanels.ingameEditor.editorUserInput;
import me.rockyhawk.commandPanels.ingameEditor.editorUtils;
import me.rockyhawk.commandPanels.openWithItem.utilsOpenWithItem;
import me.rockyhawk.commandPanels.panelBlocks.blocksTabComplete;
import me.rockyhawk.commandPanels.panelBlocks.commandpanelblocks;
import me.rockyhawk.commandPanels.panelBlocks.panelBlockOnClick;
import me.rockyhawk.commandPanels.premium.commandpanelUserInput;
import me.rockyhawk.commandPanels.premium.commandpanelrefresher;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.input.CharSequenceReader;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import javax.swing.*;

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
                    section = hasSection(panels, pconfig, Integer.parseInt(item.split("\\s")[c]), p);
                    //This section is for animations below here: VISUAL ONLY

                    //check for if there is animations inside the items section
                    if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + section + ".animate" + animateValue)) {
                        //check for if it contains the animate that has the animvatevalue
                        if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + section + ".animate" + animateValue)) {
                            section = section + ".animate" + animateValue;
                        }
                    }
                }
                ItemStack s = makeItemFromConfig(Objects.requireNonNull(pconfig.getConfigurationSection("panels." + panels + ".item." + item.split("\\s")[c] + section)), p, onOpen != 3, onOpen != 3);
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

    public ItemStack getItem(String b64stringtexture) {
        //get head from base64
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        PropertyMap propertyMap = profile.getProperties();
        if (propertyMap == null) {
            throw new IllegalStateException("Profile doesn't contain a property map");
        } else {
            propertyMap.put("textures", new Property("textures", b64stringtexture));
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            ItemMeta headMeta = head.getItemMeta();
            assert headMeta != null;
            Class headMetaClass = headMeta.getClass();

            try {
                getField(headMetaClass, "profile", GameProfile.class, 0).set(headMeta, profile);
            } catch (IllegalArgumentException | IllegalAccessException var10) {
                debug(var10);
            }

            head.setItemMeta(headMeta);
            return head;
        }
    }

    private <T> Field getField(Class<?> target, String name, Class<T> fieldType, int index) {
        Field[] var4 = target.getDeclaredFields();

        for (Field field : var4) {
            if ((name == null || field.getName().equals(name)) && fieldType.isAssignableFrom(field.getType()) && index-- <= 0) {
                field.setAccessible(true);
                return field;
            }
        }

        if (target.getSuperclass() != null) {
            return getField(target.getSuperclass(), name, fieldType, index);
        } else {
            throw new IllegalArgumentException("Cannot find field with type " + fieldType);
        }
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

    public void commandTags(Player p, String command) {
        String tag = config.getString("config.format.tag") + " ";
        //set cp placeholders
        command = papi(p, setCpPlaceholders(p, command));
        if (command.split("\\s")[0].equalsIgnoreCase("server=")) {
            //this contacts bungee and tells it to send the server change command
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(command.split("\\s")[1]);
            Player player = Bukkit.getPlayerExact(p.getName());
            assert player != null;
            player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
        } else if (command.split("\\s")[0].equalsIgnoreCase("op=")) {
            //if player uses op= it will perform command as op
            boolean isop = p.isOp();
            try {
                p.setOp(true);
                Bukkit.dispatchCommand(p,papi(p, command.replace("op=", "").trim()));
                p.setOp(isop);
            } catch (Exception exc) {
                p.setOp(isop);
                debug(exc);
                p.sendMessage(tag + papi( config.getString("config.format.error") + " op=: Error in op command!"));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("console=")) {
            //if player uses console= it will perform command in the console
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), papi(p, command.replace("console=", "").trim()));
        } else if (command.split("\\s")[0].equalsIgnoreCase("buy=")) {
            //if player uses buy= it will be eg. buy= <price> <item> <amount of item> <ID>
            try {
                if (econ != null) {
                    if (econ.getBalance(p) >= Double.parseDouble(command.split("\\s")[1])) {
                        econ.withdrawPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(config.getString("config.format.bought")).isEmpty()){
                            p.sendMessage(papi( tag + Objects.requireNonNull(config.getString("config.format.bought")).replaceAll("%cp-args%", command.split("\\s")[1])));
                        }
                        if (p.getInventory().firstEmpty() >= 0) {
                            p.getInventory().addItem(new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])));
                        } else {
                            Objects.requireNonNull(p.getLocation().getWorld()).dropItemNaturally(p.getLocation(), new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])));
                        }
                    } else {
                        p.sendMessage(papi( tag + config.getString("config.format.needmoney")));
                    }
                } else {
                    p.sendMessage(papi( tag + ChatColor.RED + "Buying Requires Vault and an Economy to work!"));
                }
            } catch (Exception buy) {
                debug(buy);
                p.sendMessage(papi(tag + config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("tokenbuy=")) {
            //if player uses tokenbuy= it will be eg. tokenbuy= <price> <item> <amount of item> <ID>
            try {
                if (this.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    assert api != null;
                    int balance = Integer.parseInt(Long.toString(api.getTokens(p).orElse(0)));
                    if (balance >= Double.parseDouble(command.split("\\s")[1])) {
                        api.removeTokens(p, Long.parseLong(command.split("\\s")[1]));
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(config.getString("config.format.bought")).isEmpty()) {
                            p.sendMessage(papi( tag + Objects.requireNonNull(config.getString("config.format.bought")).replaceAll("%cp-args%", command.split("\\s")[1])));
                        }
                        if (p.getInventory().firstEmpty() >= 0) {
                            p.getInventory().addItem(new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])));
                        } else {
                            Objects.requireNonNull(p.getLocation().getWorld()).dropItemNaturally(p.getLocation(), new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])));
                        }
                    } else {
                        p.sendMessage(papi( tag + config.getString("config.format.needmoney")));
                    }
                } else {
                    p.sendMessage(papi( tag + ChatColor.RED + "Buying Requires TokenManager to work!"));
                }
            } catch (Exception buy) {
                debug(buy);
                p.sendMessage(papi(tag + config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("sell=")) {
            //if player uses sell= it will be eg. sell= <cashback> <item> <amount of item> [enchanted:KNOCKBACK:1] [potion:JUMP]
            try {
                if (econ != null) {
                    boolean sold = false;
                    for (int f = 0; f < p.getInventory().getSize(); f++) {
                        ItemStack itm = p.getInventory().getItem(f);
                        if (itm != null && itm.getType().equals(Material.matchMaterial(command.split("\\s")[2]))) {
                            //determine if the command contains parameters for extensions
                            //String enchanted[] = {"false","0"};
                            String potion = "false";
                            for(String argsTemp : command.split("\\s")){
                                /*if(argsTemp.startsWith("enchanted:")){
                                    enchanted = argsTemp.replace("enchanted:","").split(":");
                                }*/
                                if(argsTemp.startsWith("potion:")){
                                    potion = argsTemp.replace("potion:","");
                                }
                            }
                            //check to ensure any extensions are checked
                            try {
                                /*if (!enchanted[0].equals("false")) {
                                    EnchantmentStorageMeta enchMeta = (EnchantmentStorageMeta) itm.getItemMeta();
                                    enchMeta.addStoredEnchant(Enchantment.getByKey(NamespacedKey.minecraft(enchanted[0].toLowerCase())), Integer.parseInt(enchanted[1]), false);
                                    if (enchMeta.getStoredEnchants().equals(itm.getEnchantments())) {
                                        p.sendMessage("TRUE");
                                    }
                                    if (itm.getEnchantments().containsKey(Enchantment.getByKey(NamespacedKey.minecraft(enchanted[0].toLowerCase())))) {
                                        if (itm.getEnchantmentLevel(Enchantment.getByKey(NamespacedKey.minecraft(enchanted[0].toLowerCase()))) != Integer.parseInt(enchanted[1])) {
                                            p.sendMessage(papi(p, tag + ChatColor.RED + "Your item needs Enchantment level " + enchanted[1]));
                                            return;
                                        }
                                    } else {
                                        p.sendMessage(papi(p, tag + ChatColor.RED + "Your item has the wrong enchantment"));
                                        return;
                                    }
                                }*/
                                if (!potion.equals("false")) {
                                    PotionMeta potionMeta = (PotionMeta) itm.getItemMeta();
                                    assert potionMeta != null;
                                    if (!potionMeta.getBasePotionData().getType().name().equalsIgnoreCase(potion)) {
                                        p.sendMessage(papi( tag + ChatColor.RED + "Your item has the wrong potion effect"));
                                        return;
                                    }
                                }
                            }catch(Exception exc){
                                //skip unless debug enabled
                                debug(exc);
                            }
                            if (itm.getAmount() >= new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])).getAmount()) {
                                int amt = itm.getAmount() - new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])).getAmount();
                                itm.setAmount(amt);
                                p.getInventory().setItem(f, amt > 0 ? itm : null);
                                econ.depositPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                                sold = true;
                                p.updateInventory();
                                break;
                            }
                        }
                    }
                    if (!sold) {
                        p.sendMessage(papi( tag + config.getString("config.format.needitems")));
                    } else {
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(config.getString("config.format.sold")).isEmpty()) {
                            p.sendMessage(papi( tag + Objects.requireNonNull(config.getString("config.format.sold")).replaceAll("%cp-args%", command.split("\\s")[1])));
                        }
                    }
                } else {
                    p.sendMessage(papi( tag + ChatColor.RED + "Selling Requires Vault and an Economy to work!"));
                }
            } catch (Exception sell) {
                debug(sell);
                p.sendMessage(papi(tag + config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("tokensell=")) {
            //if player uses tokensell= it will be eg. tokensell= <cashback> <item> <amount of item> [enchanted:KNOCKBACK:1] [potion:JUMP]
            try {
                if (this.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    boolean sold = false;
                    for (int f = 0; f < p.getInventory().getSize(); f++) {
                        ItemStack itm = p.getInventory().getItem(f);
                        if (itm != null && itm.getType().equals(Material.matchMaterial(command.split("\\s")[2]))) {
                            //determine if the command contains parameters for extensions
                            String potion = "false";
                            for(String argsTemp : command.split("\\s")){
                                if(argsTemp.startsWith("potion:")){
                                    potion = argsTemp.replace("potion:","");
                                }
                            }
                            //check to ensure any extensions are checked
                            try {
                                if (!potion.equals("false")) {
                                    PotionMeta potionMeta = (PotionMeta) itm.getItemMeta();
                                    assert potionMeta != null;
                                    if (!potionMeta.getBasePotionData().getType().name().equalsIgnoreCase(potion)) {
                                        p.sendMessage(papi( tag + ChatColor.RED + "Your item has the wrong potion effect"));
                                        return;
                                    }
                                }
                            }catch(Exception exc){
                                //skip if it cannot do unless debug is enabled
                                debug(exc);
                            }
                            if (itm.getAmount() >= new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])).getAmount()) {
                                int amt = itm.getAmount() - new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[2])), Integer.parseInt(command.split("\\s")[3])).getAmount();
                                itm.setAmount(amt);
                                p.getInventory().setItem(f, amt > 0 ? itm : null);
                                econ.depositPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                                assert api != null;
                                api.addTokens(p, Long.parseLong(command.split("\\s")[1]));
                                sold = true;
                                p.updateInventory();
                                break;
                            }
                        }
                    }
                    if (!sold) {
                        p.sendMessage(papi( tag + config.getString("config.format.needitems")));
                    } else {
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(config.getString("config.format.sold")).isEmpty()) {
                            p.sendMessage(papi( tag + Objects.requireNonNull(config.getString("config.format.sold")).replaceAll("%cp-args%", command.split("\\s")[1])));
                        }
                    }
                } else {
                    p.sendMessage(papi( tag + ChatColor.RED + "Selling Requires TokenManager to work!"));
                }
            } catch (Exception sell) {
                debug(sell);
                p.sendMessage(papi(tag + config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("msg=")) {
            //if player uses msg= it will send the player a message
            p.sendMessage(papi(p, command.replace("msg=", "").trim()));
        } else if (command.split("\\s")[0].equalsIgnoreCase("sound=")) {
            //if player uses sound= it will play a sound (sound= [sound])
            try {
                p.playSound(p.getLocation(), Sound.valueOf(command.split("\\s")[1]), 1F, 1F);
            } catch (Exception s) {
                debug(s);
                p.sendMessage(papi(tag + config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("tokenbuycommand=")) {
            //if player uses tokenbuycommand [price] [command]
            try {
                if (this.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    assert api != null;
                    int balance = Integer.parseInt(Long.toString(api.getTokens(p).orElse(0)));
                    if (balance >= Double.parseDouble(command.split("\\s")[1])) {
                        api.removeTokens(p, Long.parseLong(command.split("\\s")[1]));
                        //execute command under here
                        String commandp = command;
                        commandp = commandp.replace("tokenbuycommand=", "").trim();
                        String price = commandp.split(" ", 2)[0];
                        commandp = commandp.split(" ", 2)[1];
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), papi(p, commandp));
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(config.getString("config.format.bought")).isEmpty()) {
                            p.sendMessage(papi( tag + Objects.requireNonNull(config.getString("config.format.bought")).replaceAll("%cp-args%", price)));
                        }
                    } else {
                        p.sendMessage(papi( tag + config.getString("config.format.needmoney")));
                    }
                } else {
                    p.sendMessage(papi( tag + ChatColor.RED + "Buying Requires Vault and an Economy to work!"));
                }
            } catch (Exception buyc) {
                debug(buyc);
                p.sendMessage(papi(tag + config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("buycommand=")) {
            //if player uses buycommand [price] [command]
            try {
                if (econ != null) {
                    if (econ.getBalance(p) >= Double.parseDouble(command.split("\\s")[1])) {
                        econ.withdrawPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                        //execute command under here
                        String commandp = command;
                        commandp = commandp.replace("buycommand=", "").trim();
                        String price = commandp.split(" ", 2)[0];
                        commandp = commandp.split(" ", 2)[1];
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), papi( papi(p, commandp)));
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(config.getString("config.format.bought")).isEmpty()) {
                            p.sendMessage(papi( tag + Objects.requireNonNull(config.getString("config.format.bought")).replaceAll("%cp-args%", price)));
                        }
                    } else {
                        p.sendMessage(papi( tag + config.getString("config.format.needmoney")));
                    }
                } else {
                    p.sendMessage(papi( tag + ChatColor.RED + "Buying Requires Vault and an Economy to work!"));
                }
            } catch (Exception buyc) {
                debug(buyc);
                p.sendMessage(papi(tag + config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("teleport=")) {
            //if player uses teleport= x y z (optional other player)
            if (command.split("\\s").length == 6) {
                float x, y, z, yaw, pitch; //pitch is the heads Y axis and yaw is the X axis
                x = Float.parseFloat(papi(p, command.split("\\s")[1]));
                y = Float.parseFloat(papi(p, command.split("\\s")[2]));
                z = Float.parseFloat(papi(p, command.split("\\s")[3]));
                yaw = Float.parseFloat(papi(p, command.split("\\s")[4]));
                pitch = Float.parseFloat(papi(p, command.split("\\s")[5]));
                p.teleport(new Location(p.getWorld(), x, y, z, yaw, pitch));
            } else if (command.split("\\s").length <= 4) {
                float x, y, z;
                x = Float.parseFloat(papi(p, command.split("\\s")[1]));
                y = Float.parseFloat(papi(p, command.split("\\s")[2]));
                z = Float.parseFloat(papi(p, command.split("\\s")[3]));
                p.teleport(new Location(p.getWorld(), x, y, z));
            } else {
                try {
                    Player otherplayer = Bukkit.getPlayer(papi(p, command.split("\\s")[4]));
                    float x, y, z;
                    x = Float.parseFloat(papi(p, command.split("\\s")[1]));
                    y = Float.parseFloat(papi(p, command.split("\\s")[2]));
                    z = Float.parseFloat(papi(p, command.split("\\s")[3]));
                    assert otherplayer != null;
                    otherplayer.teleport(new Location(otherplayer.getWorld(), x, y, z));
                } catch (Exception tpe) {
                    p.sendMessage(tag + config.getString("config.format.notitem"));
                }
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("stopsound=")) {
            //if player uses stopsound= [sound]
            try {
                p.stopSound(Sound.valueOf(command.split("\\s")[1]));
            } catch (Exception ss) {
                debug(ss);
                p.sendMessage(papi(tag + config.getString("config.format.error") + " " + "commands: " + command));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("sudo=")) {
            //if player uses sudo= [command]
            p.chat(papi(p, "/" + command.replaceAll("sudo=", "").trim()));
        } else {
            Bukkit.dispatchCommand(p, papi(p, command));
        }
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

    public int commandPayWall(Player p, String command) { //return 0 means no funds, 1 is they passed and 2 means paywall is not this command
        String tag = config.getString("config.format.tag") + " ";
        if (command.split("\\s")[0].equalsIgnoreCase("paywall=")) {
            //if player uses paywall= [price]
            try {
                if (econ != null) {
                    if (econ.getBalance(p) >= Double.parseDouble(command.split("\\s")[1])) {
                        econ.withdrawPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                        //if the message is empty don't send
                        if(!config.getString("config.format.bought").isEmpty()) {
                            p.sendMessage(papi( tag + Objects.requireNonNull(config.getString("config.format.bought")).replaceAll("%cp-args%", command.split("\\s")[1])));
                        }
                        return 1;
                    } else {
                        p.sendMessage(papi( tag + config.getString("config.format.needmoney")));
                        return 0;
                    }
                } else {
                    p.sendMessage(papi( tag + ChatColor.RED + "Paying Requires Vault and an Economy to work!"));
                    return 0;
                }
            } catch (Exception buyc) {
                debug(buyc);
                p.sendMessage(papi(p, tag + config.getString("config.format.error") + " " + "commands: " + command));
                return 0;
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("tokenpaywall=")) {
            //if player uses tokenpaywall= [price]
            try {
                if (getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    assert api != null;
                    int balance = Integer.parseInt(Long.toString(api.getTokens(p).orElse(0)));
                    if (balance >= Double.parseDouble(command.split("\\s")[1])) {
                        api.removeTokens(p, Long.parseLong(command.split("\\s")[1]));
                        //if the message is empty don't send
                        if(!Objects.requireNonNull(config.getString("config.format.bought")).isEmpty()) {
                            p.sendMessage(papi( tag + Objects.requireNonNull(config.getString("config.format.bought")).replaceAll("%cp-args%", command.split("\\s")[1])));
                        }
                        return 1;
                    } else {
                        p.sendMessage(papi( tag + config.getString("config.format.needmoney")));
                        return 0;
                    }
                } else {
                    p.sendMessage(papi( tag + ChatColor.RED + "Paying TokenManager to work!"));
                    return 0;
                }
            } catch (Exception buyc) {
                debug(buyc);
                p.sendMessage(papi(p, tag + config.getString("config.format.error") + " " + "commands: " + command));
                return 0;
            }
        }else if (command.split("\\s")[0].equalsIgnoreCase("item-paywall=")) {
            //if player uses item-paywall= [Material] [Amount]
            try {
                ItemStack sellItem = new ItemStack(Objects.requireNonNull(Material.matchMaterial(command.split("\\s")[1])),Integer.parseInt(command.split("\\s")[2]));
                int sellItemAmount = sellItem.getAmount();
                sellItem.setAmount(1);
                int removedItem = 0;
                for(ItemStack content : p.getInventory().getContents()){
                    int contentAmount;
                    try {
                        contentAmount = content.getAmount();
                    }catch(NullPointerException skip){
                        //item is air
                        continue;
                    }
                    content.setAmount(1);
                    if(content.isSimilar(sellItem)){
                        if(sellItemAmount <= contentAmount){
                            content.setAmount(contentAmount-sellItemAmount);
                            p.updateInventory();
                            removedItem = 1;
                            break;
                        }
                    }
                    content.setAmount(contentAmount);
                }
                if(removedItem == 0){
                    p.sendMessage(papi( tag + config.getString("config.format.needmoney")));
                }else{
                    if(!Objects.requireNonNull(config.getString("config.format.sold")).isEmpty()) {
                        p.sendMessage(papi( tag + config.getString("config.format.sold")));
                    }
                }
                return removedItem;
            } catch (Exception buyc) {
                debug(buyc);
                p.sendMessage(papi(p, tag + config.getString("config.format.error") + " " + "commands: " + command));
                return 0;
            }
        }else if (command.split("\\s")[0].equalsIgnoreCase("xp-paywall=")) {
            //if player uses xp-paywall= [price]
            try {
                int balance = p.getLevel();
                if (balance >= Integer.parseInt(command.split("\\s")[1])) {
                    p.setLevel(p.getLevel() - Integer.parseInt(command.split("\\s")[1]));
                    //if the message is empty don't send
                    if(!Objects.requireNonNull(config.getString("config.format.bought")).isEmpty()) {
                        p.sendMessage(papi( tag + Objects.requireNonNull(config.getString("config.format.bought")).replaceAll("%cp-args%", command.split("\\s")[1])));
                    }
                    return 1;
                } else {
                    p.sendMessage(papi( tag + config.getString("config.format.needmoney")));
                    return 0;
                }
            } catch (Exception buyc) {
                debug(buyc);
                p.sendMessage(papi(p, tag + config.getString("config.format.error") + " " + "commands: " + command));
                return 0;
            }
        } else {
            return 2;
        }
    }

    //look through all files in all folders
    public void fileNamesFromDirectory(File directory) {
        int count = 0;
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
                panelNames.add(new String[]{tempName, Integer.toString(count)});
            }
            count += 1;
        }
    }

    public void reloadPanelFiles() {
        panelFiles.clear();
        panelNames.clear();
        //load panel files
        fileNamesFromDirectory(panelsf);
        //this bit will set openWithItem to false/true upson reload
        YamlConfiguration tempFile;
        String tempName;
        openWithItem = false;
        for(String[] panelName  : panelNames){
            tempFile = YamlConfiguration.loadConfiguration(new File(panelsf + File.separator + panelFiles.get(Integer.parseInt(panelName[1]))));
            if(!checkPanels(tempFile)){
                this.getServer().getConsoleSender().sendMessage("[CommandPanels] Error in: " + panelFiles.get(Integer.parseInt(panelName[1])));
                continue;
            }
            tempName = panelName[0];
            if(tempFile.contains("panels." + tempName + ".open-with-item")) {
                openWithItem = true;
                break;
            }
        }
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

    public void openEditorGui(Player p, int pageChange) {
        Inventory i = Bukkit.createInventory(null, 54, "Command Panels Editor");
        ArrayList<String> panelNames = new ArrayList<String>(); //all panels from ALL files (panel names)
        ArrayList<String> panelTitles = new ArrayList<String>(); //all panels from ALL files (panel titles)
        ArrayList<Material> panelItems = new ArrayList<Material>(); //all panels from ALL files (panel materials)
        try {
            for(String fileName : panelFiles) { //will loop through all the files in folder
                YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(panelsf + File.separator + fileName));
                String key;
                if (!checkPanels(temp)) {
                    continue;
                }
                for (String s : Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false)) {
                    key = s;
                    panelNames.add(papi( key));
                    panelTitles.add(papi( Objects.requireNonNull(temp.getString("panels." + key + ".title"))));
                    if (temp.contains("panels." + key + ".open-with-item.material")) {
                        panelItems.add(Material.matchMaterial(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.material"))));
                    } else {
                        panelItems.add(Material.FILLED_MAP);
                    }
                }
            }
        } catch (Exception fail) {
            //could not fetch all panel names (probably no panels exist)
            debug(fail);
            return;
        }

        int pageNumber = 1;
        if (p.getOpenInventory().getTitle().equals("Command Panels Editor")) {
            pageNumber = Integer.parseInt(ChatColor.stripColor(Objects.requireNonNull(Objects.requireNonNull(p.getOpenInventory().getItem(49)).getItemMeta()).getDisplayName()).replace("Page ", ""));
        }
        //will add the difference
        pageNumber = pageNumber + pageChange;
        if (pageNumber <= 0) {
            //double check page number IS NOT under 1
            pageNumber = 1;
        }
        //get amount of pages total
        int pagesAmount = (int) Math.ceil(panelNames.size() / 45.0);
        //make all the bottom bar items
        ItemStack temp;
        temp = new ItemStack(Material.SUNFLOWER, 1);
        setName(temp, ChatColor.WHITE + "Page " + pageNumber, null, p, true, true);
        i.setItem(49, temp);
        temp = new ItemStack(Material.BARRIER, 1);
        setName(temp, ChatColor.RED + "Exit Menu", null, p, true, true);
        i.setItem(45, temp);
        temp = new ItemStack(Material.BOOK, 1);
        List<String> lore = new ArrayList();
        lore.add(ChatColor.GRAY + "- Click on a panel to edit items.");
        lore.add(ChatColor.GRAY + "- Right click on a panel to edit settings.");
        lore.add(ChatColor.GRAY + "- To edit an item in a panel, shift click");
        lore.add(ChatColor.GRAY + "  on the item of choice.");
        lore.add(ChatColor.GRAY + "- When entering a value,");
        lore.add(ChatColor.GRAY + "  type 'remove' to set a");
        lore.add(ChatColor.GRAY + "  value to default, and use");
        lore.add(ChatColor.GRAY + "  '" + config.getString("config.input-cancel") + "' to cancel.");
        setName(temp, ChatColor.WHITE + "Panel Editor Tips", lore, p, true, true);
        i.setItem(53, temp);
        if (pageNumber != 1) {
            //only show previous page button if number is not one
            temp = new ItemStack(Material.PAPER, 1);
            setName(temp, ChatColor.WHITE + "Previous Page", null, p, true, true);
            i.setItem(48, temp);
        }
        if (pageNumber < pagesAmount) {
            //if page number is under pages amount
            temp = new ItemStack(Material.PAPER, 1);
            setName(temp, ChatColor.WHITE + "Next Page", null, p, true, true);
            i.setItem(50, temp);
        }
        int count = 0;
        int slot = 0;
        for (String panelName : panelNames) {
            //count is +1 because count starts at 0 not 1
            if ((pageNumber * 45 - 45) < (count + 1) && (pageNumber * 45) > (count)) {
                temp = new ItemStack(panelItems.get(count), 1);
                setName(temp, ChatColor.WHITE + panelName, null, p, true, true);
                i.setItem(slot, temp);
                slot += 1;
            }
            count += 1;
        }
        p.openInventory(i);
    }

    public void openPanelSettings(Player p, String panelName, YamlConfiguration cf) {
        Inventory i = Bukkit.createInventory(null, 45, "Panel Settings: " + panelName);
        List<String> lore = new ArrayList();
        ItemStack temp;
        //remove if the player already had a string from previously
        for (int o = 0; editorInputStrings.size() > o; o++) {
            if (editorInputStrings.get(o)[0].equals(p.getName())) {
                editorInputStrings.remove(o);
                o = o - 1;
            }
        }
        //make all the items
        temp = new ItemStack(Material.WRITABLE_BOOK, 1);
        lore.add(ChatColor.GRAY + "Permission required to open panel");
        lore.add(ChatColor.GRAY + "commandpanel.panel.[insert]");
        if (cf.contains("panels." + panelName + ".perm")) {
            lore.add(ChatColor.WHITE + "--------------------------------");
            lore.add(ChatColor.WHITE + "commandpanel.panel." + cf.getString("panels." + panelName + ".perm"));
        }
        setName(temp, ChatColor.WHITE + "Panel Permission", lore, p,true, true);
        i.setItem(1, temp);

        temp = new ItemStack(Material.NAME_TAG, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Title of the Panel");
        if (cf.contains("panels." + panelName + ".title")) {
            lore.add(ChatColor.WHITE + "------------------");
            lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".title"));
        }
        setName(temp, ChatColor.WHITE + "Panel Title", lore, p,true, true);
        i.setItem(3, temp);

        temp = new ItemStack(Material.JUKEBOX, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Sound when opening panel");
        if (cf.contains("panels." + panelName + ".sound-on-open")) {
            lore.add(ChatColor.WHITE + "------------------------");
            lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("panels." + panelName + ".sound-on-open")).toUpperCase());
        }
        setName(temp, ChatColor.WHITE + "Panel Sound", lore, p,true, true);
        i.setItem(5, temp);

        temp = new ItemStack(Material.IRON_DOOR, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Custom command to open panel");
        if (cf.contains("panels." + panelName + ".command")) {
            lore.add(ChatColor.WHITE + "----------------------------");
            lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".command"));
        }
        setName(temp, ChatColor.WHITE + "Panel Command", lore, p,true, true);
        i.setItem(7, temp);

        temp = new ItemStack(Material.LAVA_BUCKET, 1);
        lore.clear();
        lore.add(ChatColor.DARK_RED + "Permanently delete Panel");
        setName(temp, ChatColor.RED + "Delete Panel", lore, p,true, true);
        i.setItem(21, temp);

        temp = new ItemStack(Material.PISTON, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "How many rows the panel will be");
        lore.add(ChatColor.GRAY + "choose an integer from 1 to 6");
        setName(temp, ChatColor.WHITE + "Panel Rows", lore, p,true, true);
        i.setItem(23, temp);

        temp = new ItemStack(Material.BLACK_STAINED_GLASS, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Fill empty slots with an item");
        if (cf.contains("panels." + panelName + ".empty")) {
            lore.add(ChatColor.WHITE + "-----------------------");
            lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("panels." + panelName + ".empty")).toUpperCase());
        }
        setName(temp, ChatColor.WHITE + "Panel Empty Item", lore, p,true, true);
        i.setItem(13, temp);

        temp = new ItemStack(Material.COMMAND_BLOCK, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Execute commands when opening");
        lore.add(ChatColor.GRAY + "- Left click to add command");
        lore.add(ChatColor.GRAY + "- Right click to remove command");
        if (cf.contains("panels." + panelName + ".commands-on-open")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("panels." + panelName + ".commands-on-open")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        setName(temp, ChatColor.WHITE + "Panel Commands", lore, p,true, true);
        i.setItem(15, temp);

        temp = new ItemStack(Material.ITEM_FRAME, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Code name to open panel");
        lore.add(ChatColor.GRAY + "/cp [name]");
        lore.add(ChatColor.WHITE + "-----------------------");
        lore.add(ChatColor.WHITE + panelName);
        setName(temp, ChatColor.WHITE + "Panel Name", lore, p,true, true);
        i.setItem(11, temp);

        temp = new ItemStack(Material.BARRIER, 1);
        setName(temp, ChatColor.RED + "Back", null, p,true, true);
        i.setItem(18, temp);

        //This will create a wall of glass panes, separating panel settings with hotbar settings
        temp = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        setName(temp, ChatColor.WHITE + "", null, p,false, true);
        for(int d = 27; d < 36; d++){
            i.setItem(d, temp);
        }
        //This is the items for hotbar items (open-with-item)
        boolean hotbarItems = false;

        if(cf.contains("panels." + panelName + ".open-with-item.material")){
            hotbarItems = true;
            temp = new ItemStack((Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(cf.getString("panels." + panelName + ".open-with-item.material"))))), 1);
        }else{
            temp = new ItemStack(Material.REDSTONE_BLOCK, 1);
        }
        lore.clear();
        lore.add(ChatColor.GRAY + "Current Item");
        if (cf.contains("panels." + panelName + ".open-with-item.material")) {
            lore.add(ChatColor.WHITE + "-----------------------");
            lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("panels." + panelName + ".open-with-item.material")).toUpperCase());
        }else{
            lore.add(ChatColor.WHITE + "-----------------------");
            lore.add(ChatColor.RED + "DISABLED");
        }
        setName(temp, ChatColor.WHITE + "Panel Hotbar Item", lore, p,true, true);
        i.setItem(40, temp);

        if(hotbarItems) {
            temp = new ItemStack(Material.NAME_TAG, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Name for Hotbar item");
            if (cf.contains("panels." + panelName + ".open-with-item.name")) {
                lore.add(ChatColor.WHITE + "----------");
                lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("panels." + panelName + ".open-with-item.name")));
            }
            setName(temp, ChatColor.WHITE + "Hotbar Item Name", lore, p, true, true);
            i.setItem(38, temp);

            temp = new ItemStack(Material.SPRUCE_SIGN, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Display a lore under the Hotbar item");
            lore.add(ChatColor.GRAY + "- Left click to add lore");
            lore.add(ChatColor.GRAY + "- Right click to remove lore");
            if (cf.contains("panels." + panelName + ".open-with-item.lore")) {
                lore.add(ChatColor.WHITE + "-------------------------------");
                int count = 1;
                for (String tempLore : cf.getStringList("panels." + panelName + ".open-with-item.lore")) {
                    lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                    count += 1;
                }
            }
            setName(temp, ChatColor.WHITE + "Hotbar Lore", lore, p,true, true);
            i.setItem(36, temp);

            temp = new ItemStack(Material.BEDROCK, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Hotbar location for the item");
            lore.add(ChatColor.GRAY + "choose a number from 1 to 9");
            if (cf.contains("panels." + panelName + ".open-with-item.stationary")) {
                lore.add(ChatColor.WHITE + "-------------------------");
                //in the editor, change the value of 0-8 to 1-9 for simplicity
                int location = cf.getInt("panels." + panelName + ".open-with-item.stationary") + 1;
                lore.add(ChatColor.WHITE + String.valueOf(location));
            }
            setName(temp, ChatColor.WHITE + "Hotbar Item Location", lore, p, true, true);
            i.setItem(42, temp);

            temp = new ItemStack(Material.BOOK, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "- To refresh changes use");
            lore.add(ChatColor.GRAY + "  /cp " + panelName + " item");
            lore.add(ChatColor.GRAY + "- Hotbar items will need a");
            lore.add(ChatColor.GRAY + "  name to work properly.");
            setName(temp, ChatColor.WHITE + "Hotbar Item Tips", lore, p, true, true);
            i.setItem(44, temp);
        }

        p.openInventory(i);
    }

    public void openItemSettings(Player p, String panelName, YamlConfiguration cf, int itemNumber) {
        Inventory i = Bukkit.createInventory(null, 36, "Item Settings: " + panelName);
        List<String> lore = new ArrayList();
        ItemStack temp;
        //remove if the player already had a string from previously
        for (int o = 0; editorInputStrings.size() > o; o++) {
            if (editorInputStrings.get(o)[0].equals(p.getName())) {
                editorInputStrings.remove(o);
                o = o - 1;
            }
        }
        //make all the items
        temp = new ItemStack(Material.NAME_TAG, 1);
        lore.add(ChatColor.GRAY + "Display name of the item in the Panel");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".name")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".name"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".name"));
            }
        }
        setName(temp, ChatColor.WHITE + "Item Name", lore, p,true, true);
        i.setItem(1, temp);

        temp = new ItemStack(Material.COMMAND_BLOCK, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Execute commands when item is clicked");
        lore.add(ChatColor.GRAY + "- Left click to add command");
        lore.add(ChatColor.GRAY + "- Right click to remove command");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".commands")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("panels." + panelName + ".item." + itemNumber + ".commands")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        setName(temp, ChatColor.WHITE + "Item Commands", lore, p,true, true);
        i.setItem(3, temp);

        temp = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Display enchantment of the item in the Panel");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".enchanted")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".name"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".enchanted"));
            }
        } else {
            lore.add(ChatColor.WHITE + "--------------------------------");
            lore.add(ChatColor.WHITE + "false");
        }
        setName(temp, ChatColor.WHITE + "Item Enchantment", lore, p,true, true);
        i.setItem(5, temp);

        temp = new ItemStack(Material.POTION, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Display potion effect of the item in the Panel");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".potion")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".potion"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".potion"));
            }
        }
        setName(temp, ChatColor.WHITE + "Item Potion Effect", lore, p,true, true);
        i.setItem(7, temp);

        temp = new ItemStack(Material.SPRUCE_SIGN, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Display a lore under the item name");
        lore.add(ChatColor.GRAY + "- Left click to add lore line");
        lore.add(ChatColor.GRAY + "- Right click to remove lore line");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".lore")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("panels." + panelName + ".item." + itemNumber + ".lore")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        setName(temp, ChatColor.WHITE + "Item Lores", lore, p, true, true);
        i.setItem(19, temp);

        temp = new ItemStack(Material.ITEM_FRAME, 2);
        lore.clear();
        lore.add(ChatColor.GRAY + "How many of the item will be stacked");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".stack")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".stack"), "")) {
                try {
                    temp.setAmount(Integer.parseInt(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".stack"))));
                } catch (Exception ignored) {
                }
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".stack"));
            }
        }
        setName(temp, ChatColor.WHITE + "Item Stack Size", lore, p, true, true);
        i.setItem(21, temp);

        temp = new ItemStack(Material.ANVIL, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Add Custom Model Data here");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".customdata")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".customdata"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".customdata"));
            }
        }
        setName(temp, ChatColor.WHITE + "Custom Model Data", lore, p, true, true);
        i.setItem(23, temp);

        temp = new ItemStack(Material.LEATHER_HELMET, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Choose a colour for the armor");
        lore.add(ChatColor.GRAY + "use r,g,b or a spigot API color");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".leatherarmor")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".leatherarmor"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".leatherarmor"));
            }
        }
        setName(temp, ChatColor.WHITE + "Leather Armor Colour", lore, p, true, true);
        i.setItem(25, temp);

        temp = new ItemStack(Material.BARRIER, 1);
        setName(temp, ChatColor.RED + "Back", null, p, true, true);
        i.setItem(27, temp);

        if(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")).startsWith("cps=")){
            temp = new ItemStack(Material.PLAYER_HEAD, 1);
            if(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")).equalsIgnoreCase("cps= self")){
                //if self
                SkullMeta meta = (SkullMeta) temp.getItemMeta();
                try {
                    assert meta != null;
                    meta.setOwningPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()));
                } catch (Exception var23) {
                    debug(var23);
                }
                temp.setItemMeta(meta);
            }else{
                //custom head
                temp = this.getItem(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")).replace("cps=", "").trim());
            }
        }else if (Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")).startsWith("%cp-player-online-")){
            //leave default for the find material tag
            temp = new ItemStack(Material.PLAYER_HEAD, 1);
        }else if (Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")).startsWith("hdb=")){
            //head database head
            temp = new ItemStack(Material.PLAYER_HEAD, 1);
            if (this.getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
                HeadDatabaseAPI api;
                api = new HeadDatabaseAPI();
                try {
                    temp = api.getItemHead(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")).replace("hdb=", "").trim());
                } catch (Exception var22) {
                    debug(var22);
                }
            }
        }else{
            temp = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")))), 1);
        }
        try {
            temp.setAmount(Integer.parseInt(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".stack"))));
        } catch (Exception ex) {
            //skip
        }
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".enchanted")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".enchanted"), "false")) {
                ItemMeta EnchantMeta;
                EnchantMeta = temp.getItemMeta();
                assert EnchantMeta != null;
                EnchantMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                EnchantMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                temp.setItemMeta(EnchantMeta);
            }
        }
        lore.clear();
        lore.add(ChatColor.GRAY + "Click to set custom material");
        lore.add(ChatColor.GRAY + "typically for custom heads");
        setName(temp, ChatColor.WHITE + "Item Slot " + itemNumber + " Preview", lore, p, true, true);
        i.setItem(35, temp);

        p.openInventory(i);
    }

    public Reader getReaderFromStream(InputStream initialStream) throws IOException {
        //this reads the encrypted resource files in the jar file
        byte[] buffer = IOUtils.toByteArray(initialStream);
        return new CharSequenceReader(new String(buffer));
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

    public ItemStack makeItemFromConfig(ConfigurationSection itemSection, Player p, boolean placeholders, boolean colours){
        String tag = this.config.getString("config.format.tag") + " ";
        String material = itemSection.getString("material");
        try {
            if (Objects.requireNonNull(itemSection.getString("material")).equalsIgnoreCase("AIR")) {
                return null;
            }
        }catch(NullPointerException e){
            debug(e);
            p.sendMessage(papi(tag + this.config.getString("config.format.error") + " material: could not load material!"));
            return null;
        }
        ItemStack s;
        String mat;
        String matskull;
        String skullname;
        //this will convert the %cp-player-online-1-find% into cps= NAME
        assert material != null;
        if (material.contains("%cp-player-online-")) {
            int start = material.indexOf("%cp-player-online-");
            int end = material.lastIndexOf("-find%");
            String playerLocation = material.substring(start, end).replace("%cp-player-online-", "");
            Player[] playerFind = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
            if (Integer.parseInt(playerLocation) > playerFind.length) {
                material = material.replace(material.substring(start, end) + "-find%", "cps= " + config.getString("config.format.offlineHeadValue"));
            } else {
                material = material.replace(material.substring(start, end) + "-find%", "cpo= " + playerFind[Integer.parseInt(playerLocation) - 1].getName());
                //cpo is to get the skull of the player online. It is fine since the plugin knows the player is online
            }
        }
        try {
            mat = material.toUpperCase();
            matskull = material;
            skullname = "no skull";
            if (matskull.split("\\s")[0].toLowerCase().equals("cps=") || matskull.split("\\s")[0].toLowerCase().equals("cpo=")) {
                skullname = p.getUniqueId().toString();
                mat = "PLAYER_HEAD";
            }

            if (matskull.split("\\s")[0].toLowerCase().equals("hdb=")) {
                skullname = "hdb";
                mat = "PLAYER_HEAD";
            }

            s = new ItemStack(Objects.requireNonNull(Material.matchMaterial(mat)), 1);

            if (!skullname.equals("no skull") && !skullname.equals("hdb") && !matskull.split("\\s")[0].equalsIgnoreCase("cpo=")) {
                try {
                    SkullMeta meta;
                    if (matskull.split("\\s")[1].equalsIgnoreCase("self")) {
                        //if self/own
                        meta = (SkullMeta) s.getItemMeta();
                        try {
                            assert meta != null;
                            meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(skullname)));
                        } catch (Exception var23) {
                            p.sendMessage(papi( tag + this.config.getString("config.format.error") + " material: cps= self"));
                            debug(var23);
                        }
                        s.setItemMeta(meta);
                    } else {
                        //custom data
                        s = this.getItem(matskull.split("\\s")[1]);
                    }
                } catch (Exception var32) {
                    p.sendMessage(papi( tag + this.config.getString("config.format.error") + " head material: Could not load skull"));
                    debug(var32);
                }
            }
            if (!skullname.equals("no skull") && matskull.split("\\s")[0].equalsIgnoreCase("cpo=")) {
                SkullMeta cpoMeta = (SkullMeta) s.getItemMeta();
                assert cpoMeta != null;
                cpoMeta.setOwningPlayer(Bukkit.getOfflinePlayer(Objects.requireNonNull(Bukkit.getPlayer(matskull.split("\\s")[1])).getUniqueId()));
                s.setItemMeta(cpoMeta);
            }

            if (skullname.equals("hdb")) {
                if (this.getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
                    HeadDatabaseAPI api;
                    api = new HeadDatabaseAPI();

                    try {
                        s = api.getItemHead(matskull.split("\\s")[1].trim());
                    } catch (Exception var22) {
                        p.sendMessage(papi(tag + this.config.getString("config.format.error") + " hdb: could not load skull!"));
                        debug(var22);
                    }
                } else {
                    p.sendMessage(papi(tag + "Download HeadDatabaseHook from Spigot to use this feature!"));
                }
            }
            if (itemSection.contains("map")) {
                /*
                This will do maps from custom images
                the maps will be in the 'maps' folder, so
                CommandPanels/maps/image.png <-- here
                Commandpanels/panels/example.yml
                The images should be 128x128
                 */
                try{
                    @SuppressWarnings("deprecation")
                    MapView map = Bukkit.getServer().getMap(0);
                    try {
                        map.getRenderers().clear();
                        map.setCenterX(30000000);
                        map.setCenterZ(30000000);
                    }catch(NullPointerException ignore){
                        //ignore catch
                    }
                    if(new File(getDataFolder().getPath() + File.separator + "maps" + File.separator + itemSection.getString("map")).exists()) {
                        map.addRenderer(new MapRenderer() {
                            public void render(MapView view, MapCanvas canvas, Player player) {
                                canvas.drawImage(0, 0, new ImageIcon(getDataFolder().getPath() + File.separator + "maps" + File.separator + itemSection.getString("map")).getImage());
                            }
                        });
                        MapMeta meta = (MapMeta) s.getItemMeta();
                        meta.setMapView(map);
                        s.setItemMeta(meta);
                    }else{
                        p.sendMessage(papi(tag + this.config.getString("config.format.error") + " map: File not found."));
                    }
                }catch(Exception map){
                    p.sendMessage(papi(tag + this.config.getString("config.format.error") + " map: " + itemSection.getString("map")));
                    debug(map);
                }
            }
            if (itemSection.contains("enchanted")) {
                try {
                    ItemMeta EnchantMeta;
                    if (Objects.requireNonNull(itemSection.getString("enchanted")).trim().equalsIgnoreCase("true")) {
                        EnchantMeta = s.getItemMeta();
                        assert EnchantMeta != null;
                        EnchantMeta.addEnchant(Enchantment.KNOCKBACK, 1, false);
                        EnchantMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        s.setItemMeta(EnchantMeta);
                    } else if (!Objects.requireNonNull(itemSection.getString("enchanted")).trim().equalsIgnoreCase("false")) {
                        EnchantMeta = s.getItemMeta();
                        assert EnchantMeta != null;
                        EnchantMeta.addEnchant(Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(Objects.requireNonNull(itemSection.getString("enchanted")).split("\\s")[0].toLowerCase()))), Integer.parseInt(Objects.requireNonNull(itemSection.getString("enchanted")).split("\\s")[1]), true);
                        s.setItemMeta(EnchantMeta);
                    }
                } catch (Exception ench) {
                    p.sendMessage(papi(tag + this.config.getString("config.format.error") + " enchanted: " + itemSection.getString("enchanted")));
                    debug(ench);
                }
            }
            if (itemSection.contains("customdata")) {
                ItemMeta customMeta = s.getItemMeta();
                assert customMeta != null;
                customMeta.setCustomModelData(Integer.parseInt(Objects.requireNonNull(itemSection.getString("customdata"))));
                s.setItemMeta(customMeta);
            }
            if (itemSection.contains("leatherarmor")) {
                //if the item is leather armor, change the colour to this
                try {
                    if (s.getType() == Material.LEATHER_BOOTS || s.getType() == Material.LEATHER_LEGGINGS || s.getType() == Material.LEATHER_CHESTPLATE || s.getType() == Material.LEATHER_HELMET) {
                        LeatherArmorMeta leatherMeta = (LeatherArmorMeta) s.getItemMeta();
                        String colourCode = itemSection.getString("leatherarmor");
                        assert colourCode != null;
                        if (!colourCode.contains(",")) {
                            //use a color name
                            assert leatherMeta != null;
                            leatherMeta.setColor(colourCodes.get(colourCode.toUpperCase()));
                        } else {
                            //use RGB sequence
                            int[] colorRGB = {255, 255, 255};
                            int count = 0;
                            for (String colourNum : colourCode.split(",")) {
                                colorRGB[count] = Integer.parseInt(colourNum);
                                count += 1;
                            }
                            assert leatherMeta != null;
                            leatherMeta.setColor(Color.fromRGB(colorRGB[0], colorRGB[1], colorRGB[2]));
                        }
                        s.setItemMeta(leatherMeta);
                    }
                } catch (Exception er) {
                    //don't colour the armor
                    debug(er);
                    p.sendMessage(papi(tag + this.config.getString("config.format.error") + " leatherarmor: " + itemSection.getString("leatherarmor")));
                }
            }
            if (itemSection.contains("potion")) {
                //if the item is a potion, give it an effect
                try {
                    PotionMeta potionMeta = (PotionMeta)s.getItemMeta();
                    String effectType = itemSection.getString("potion");
                    assert potionMeta != null;
                    assert effectType != null;
                    potionMeta.setBasePotionData(new PotionData(PotionType.valueOf(effectType.toUpperCase())));
                    potionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                    s.setItemMeta(potionMeta);
                } catch (Exception er) {
                    //don't add the effect
                    debug(er);
                    p.sendMessage(papi(tag + ChatColor.RED + this.config.getString("config.format.error") + " potion: " + itemSection.getString("potion")));
                }
            }
            if (itemSection.contains("damage")) {
                //change the damage amount (placeholders accepted)
                try {
                    Damageable itemDamage = (Damageable) s.getItemMeta();
                    itemDamage.setDamage(Integer.parseInt(Objects.requireNonNull(papi(p, itemSection.getString("damage")))));
                    s.setItemMeta((ItemMeta) itemDamage);
                }catch(Exception e){
                    debug(e);
                    p.sendMessage(papi(tag + this.config.getString("config.format.error") + " damage: " + itemSection.getString("damage")));
                }
            }
            if (itemSection.contains("stack")) {
                //change the stack amount (placeholders accepted)
                s.setAmount(Integer.parseInt(Objects.requireNonNull(papi(p,itemSection.getString("stack")))));
            }
        } catch (IllegalArgumentException | NullPointerException var33) {
            debug(var33);
            p.sendMessage(papi(tag + this.config.getString("config.format.error") + " material: " + itemSection.getString("material")));
            return null;
        }
        this.setName(s, itemSection.getString("name"), itemSection.getStringList("lore"), p, placeholders, colours);
        return s;
    }

    //hasperm hasvalue, etc sections will be done here
    public String hasSection(String panelName, YamlConfiguration cf, int slot, Player p){
        if (cf.contains("panels." + panelName + ".item." + slot + ".hasvalue")) {
            //loop through possible hasvalue 1,2,3,etc
            for (int count = 0; Objects.requireNonNull(cf.getConfigurationSection("panels." + panelName + ".item." + slot)).getKeys(false).size() > count; count++) {
                if (cf.contains("panels." + panelName + ".item." + slot + ".hasvalue" + count)) {
                    boolean outputValue = true;
                    //outputValue will default to true
                    if (cf.contains("panels." + panelName + ".item." + slot + ".hasvalue" + count + ".output")) {
                        //if output is true, and values match it will be this item, vice versa
                        outputValue = cf.getBoolean("panels." + panelName + ".item." + slot + ".hasvalue" + count + ".output");
                    }
                    String value = cf.getString("panels." + panelName + ".item." + slot + ".hasvalue" + count + ".value");
                    String compare = ChatColor.stripColor(papi(p,setCpPlaceholders(p,cf.getString("panels." + panelName + ".item." + slot + ".hasvalue" + count + ".compare"))));
                    if (compare.equals(value) == outputValue) {
                        //onOpen being 3 means it is the editor panel.. hasvalue items cannot be included to avoid item breaking
                        return ".hasvalue" + count;
                    }
                }
            }
            //this will do the hasvalue without any numbers
            boolean outputValue = true;
            //outputValue will default to true
            if (cf.contains("panels." + panelName + ".item." + slot + ".hasvalue.output")) {
                //if output is true, and values match it will be this item, vice versa
                outputValue = cf.getBoolean("panels." + panelName + ".item." + slot + ".hasvalue.output");
            }
            String value = cf.getString("panels." + panelName + ".item." + slot + ".hasvalue.value");
            String compare = ChatColor.stripColor(papi(p,setCpPlaceholders(p,cf.getString("panels." + panelName + ".item." + slot + ".hasvalue.compare"))));
            if (compare.equals(value) == outputValue) {
                //onOpen being 3 means it is the editor panel.. hasvalue items cannot be included to avoid item breaking
                return ".hasvalue";
            }
        }
        if (cf.contains("panels." + panelName + ".item." + slot + ".hasgreater")) {
            //this will do the hasgreater without any numbers
            boolean outputValue = true;
            //outputValue will default to true
            if (cf.contains("panels." + panelName + ".item." + slot + ".hasgreater.output")) {
                //if output is true, and values match it will be this item, vice versa
                outputValue = cf.getBoolean("panels." + panelName + ".item." + slot + ".hasgreater.output");
            }
            int value = cf.getInt("panels." + panelName + ".item." + slot + ".hasgreater.value");
            double compare = Double.parseDouble(ChatColor.stripColor(papi(p,setCpPlaceholders(p,cf.getString("panels." + panelName + ".item." + slot + ".hasgreater.compare")))));
            if ((compare >= value) == outputValue) {
                //onOpen being 3 means it is the editor panel.. hasgreater items cannot be included to avoid item breaking
                return ".hasgreater";
            }
            //loop through possible hasgreater 1,2,3,etc
            for (int count = 0; Objects.requireNonNull(cf.getConfigurationSection("panels." + panelName + ".item." + slot)).getKeys(false).size() > count; count++) {
                if (cf.contains("panels." + panelName + ".item." + slot + ".hasgreater" + count)) {
                    outputValue = true;
                    //outputValue will default to true
                    if (cf.contains("panels." + panelName + ".item." + slot + ".hasgreater" + count + ".output")) {
                        //if output is true, and values match it will be this item, vice versa
                        outputValue = cf.getBoolean("panels." + panelName + ".item." + slot + ".hasgreater" + count + ".output");
                    }
                    value = cf.getInt("panels." + panelName + ".item." + slot + ".hasgreater" + count + ".value");
                    compare = Double.parseDouble(ChatColor.stripColor(papi(p,setCpPlaceholders(p,cf.getString("panels." + panelName + ".item." + slot + ".hasgreater" + count + ".compare")))));
                    if ((compare >= value) == outputValue) {
                        //onOpen being 3 means it is the editor panel.. hasgreater items cannot be included to avoid item breaking
                        return ".hasgreater" + count;
                    }
                }
            }
        }
        if (cf.contains("panels." + panelName + ".item." + slot + ".hasperm")) {
            //this will do hasperm with no numbers
            boolean outputValue = true;
            //outputValue will default to true
            if (cf.contains("panels." + panelName + ".item." + slot + ".hasperm" + ".output")) {
                //if output is true, and values match it will be this item, vice versa
                outputValue = cf.getBoolean("panels." + panelName + ".item." + slot + ".hasperm" + ".output");
            }
            if (p.hasPermission(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + slot + ".hasperm.perm"))) == outputValue) {
                return ".hasperm";
            }
            for(int count = 0; Objects.requireNonNull(cf.getConfigurationSection("panels." + panelName + ".item." + slot)).getKeys(false).size() > count; count++){
                if (cf.contains("panels." + panelName + ".item." + slot + ".hasperm" + count) && cf.contains("panels." + panelName + ".item." + slot + ".hasperm"  + count + ".perm")) {
                    outputValue = true;
                    //outputValue will default to true
                    if (cf.contains("panels." + panelName + ".item." + slot + ".hasperm" + count + ".output")) {
                        //if output is true, and values match it will be this item, vice versa
                        outputValue = cf.getBoolean("panels." + panelName + ".item." + slot + ".hasperm" + count + ".output");
                    }
                    if (p.hasPermission(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + slot + ".hasperm" + count + ".perm"))) == outputValue) {
                        return ".hasperm" + count;
                    }
                }
            }
        }
        return "";
    }

    //this is the main method to open a panel
    public void openCommandPanel(CommandSender sender, Player p, String panels, YamlConfiguration cf, boolean sendOpenedMessage){
        String tag = config.getString("config.format.tag") + " ";
        if (sender.hasPermission("commandpanel.panel." + cf.getString("panels." + panels + ".perm"))) {
            //if the sender has OTHER perms, or if sendOpenedMessage is false, implying it is not for another person
            if(sender.hasPermission("commandpanel.other") || !sendOpenedMessage) {
                try {
                    if (cf.contains("panels." + panels + ".disabled-worlds")) {
                        List<String> disabledWorlds = cf.getStringList("panels." + panels + ".disabled-worlds");
                        if (disabledWorlds.contains(p.getWorld().getName())) {
                            //panel cannot be used in the players world!
                            if (Objects.requireNonNull(config.getString("config.disabled-world-message")).equalsIgnoreCase("true")) {
                                sender.sendMessage(papi(tag + ChatColor.RED + "Panel is disabled in the world!"));
                            }
                            return;
                        }
                    }
                }catch(NullPointerException offlinePlayer){
                    //SKIP because player is offline
                    sender.sendMessage(papi(tag + config.getString("config.format.notitem")));
                    return;
                }
                try {
                    if (cf.contains("panels." + panels + ".sound-on-open")) {
                        //play sound when panel is opened
                        if(!Objects.requireNonNull(cf.getString("panels." + panels + ".sound-on-open")).equalsIgnoreCase("off")) {
                            try {
                                p.playSound(p.getLocation(), Sound.valueOf(Objects.requireNonNull(cf.getString("panels." + panels + ".sound-on-open")).toUpperCase()), 1F, 1F);
                            } catch (Exception s) {
                                p.sendMessage(papi(tag + config.getString("config.format.error") + " " + "sound-on-open: " + cf.getString("panels." + panels + ".sound-on-open")));
                            }
                        }
                    }
                    if (cf.contains("panels." + panels + ".commands-on-open")) {
                        //execute commands on panel open
                        try {
                            List<String> commands = cf.getStringList("panels." + panels + ".commands-on-open");
                            for (int i = 0; commands.size() - 1 >= i; i++) {
                                int val = commandPayWall(p,commands.get(i));
                                if(val == 0){
                                    break;
                                }
                                if(val == 2){
                                    commandTags(p, commands.get(i));
                                }
                            }
                        }catch(Exception s){
                            p.sendMessage(papi(tag + config.getString("config.format.error") + " " + "commands-on-open: " + cf.getString("panels." + panels + ".commands-on-open")));
                        }
                    }
                    openGui(panels, p, cf,1,0);
                    if(sendOpenedMessage) {
                        sender.sendMessage(papi( tag + ChatColor.GREEN + "Panel Opened for " + p.getDisplayName()));
                    }
                } catch (Exception r) {
                    debug(r);
                    sender.sendMessage(papi(tag + config.getString("config.format.notitem")));
                }
            }else{
                sender.sendMessage(papi(tag + config.getString("config.format.perms")));
            }
            return;
        }
        sender.sendMessage(papi(tag + config.getString("config.format.perms")));
    }

    //this will give a hotbar item to a player
    public void giveHotbarItem(CommandSender sender, Player p, String panels, YamlConfiguration cf, boolean sendGiveMessage){
        String tag = config.getString("config.format.tag") + " ";
        if (sender.hasPermission("commandpanel.item." + cf.getString("panels." + panels + ".perm")) && cf.contains("panels." + panels + ".open-with-item")) {
            try {
                if (cf.contains("panels." + panels + ".disabled-worlds")) {
                    List<String> disabledWorlds = cf.getStringList("panels." + panels + ".disabled-worlds");
                    if (disabledWorlds.contains(p.getWorld().getName())) {
                        //panel cannot be used in the players world!
                        if (Objects.requireNonNull(config.getString("config.disabled-world-message")).equalsIgnoreCase("true")) {
                            sender.sendMessage(papi(tag + ChatColor.RED + "Panel is disabled in the world!"));
                        }
                        return;
                    }
                }
            }catch(NullPointerException offlinePlayer){
                //SKIP because player is offline
                sender.sendMessage(papi(tag + config.getString("config.format.notitem")));
                return;
            }
            ItemStack s;
            try {
                s = makeItemFromConfig(Objects.requireNonNull(cf.getConfigurationSection("panels." + panels + ".open-with-item")), p, false, true);
            }catch(Exception n){
                sender.sendMessage(papi(tag + config.getString("config.format.error") + " open-with-item: material"));
                return;
            }
            setName(s, cf.getString("panels." + panels + ".open-with-item.name"), cf.getStringList("panels." + panels + ".open-with-item.lore"),p,false, true);
            //if the sender has OTHER perms, or if sendGiveMessage is false, implying it is not for another person
            if(sender.hasPermission("commandpanel.other") || !sendGiveMessage) {
                try {
                    if(cf.contains("panels." + panels + ".open-with-item.stationary")) {
                        p.getInventory().setItem(Integer.parseInt(Objects.requireNonNull(cf.getString("panels." + panels + ".open-with-item.stationary"))), s);
                    }else{
                        p.getInventory().addItem(s);
                    }
                    if(sendGiveMessage) {
                        sender.sendMessage(papi( tag + ChatColor.GREEN + "Item Given to " + p.getDisplayName()));
                    }
                } catch (Exception r) {
                    sender.sendMessage(papi(tag + config.getString("config.format.notitem")));
                }
            }else{
                sender.sendMessage(papi(tag + config.getString("config.format.perms")));
            }
            return;
        }
        if (!cf.contains("panels." + panels + ".open-with-item")) {
            sender.sendMessage(papi(tag + config.getString("config.format.noitem")));
            return;
        }
        sender.sendMessage(papi(tag + config.getString("config.format.perms")));
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
