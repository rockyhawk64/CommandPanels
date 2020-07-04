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
import java.net.URL;
import java.util.*;

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
import me.rockyhawk.commandPanels.premium.commandpanelUserInput;
import me.rockyhawk.commandPanels.premium.commandpanelrefresher;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.input.CharSequenceReader;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class commandpanels extends JavaPlugin {
    public YamlConfiguration config;
    public Economy econ = null;
    public boolean update = false;
    public boolean debug = false;
    public List<String> panelRunning = new ArrayList();
    public List<String[]> userInputStrings = new ArrayList();
    public List<String[]> editorInputStrings = new ArrayList();
    public ArrayList<YamlConfiguration> panelFiles = new ArrayList<YamlConfiguration>();
    public ArrayList<String[]> panelNames = new ArrayList<String[]>(); //this will return something like {"mainMenuPanel","4"} which means the 4 is for panelFiles.get(4). So you know which file it is for
    public File panelsf;

    public commandpanels() {
        this.panelsf = new File(this.getDataFolder() + File.separator + "panels");
    }

    public void onEnable() {
        this.config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + File.separator + "config.yml"));
        Bukkit.getLogger().info("[CommandPanels] RockyHawk's CommandPanels v" + this.getDescription().getVersion() + " Plugin Loading...");
        this.setupEconomy();
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        new Metrics(this);
        this.getCommand("commandpanel").setExecutor(new commandpanel(this));
        this.getCommand("commandpanel").setTabCompleter(new cpTabComplete(this));
        this.getCommand("commandpanelgenerate").setTabCompleter(new tabCompleteGenerate(this));
        this.getCommand("commandpaneledit").setTabCompleter(new cpTabCompleteIngame(this));
        this.getCommand("commandpanelgenerate").setExecutor(new commandpanelsgenerate(this));
        this.getCommand("commandpanelreload").setExecutor(new commandpanelsreload(this));
        this.getCommand("commandpaneldebug").setExecutor(new commandpanelsdebug(this));
        this.getCommand("commandpanelclose").setExecutor(new commandpanelclose(this));
        this.getCommand("commandpanelversion").setExecutor(new commandpanelversion(this));
        this.getCommand("commandpanellist").setExecutor(new commandpanelslist(this));
        this.getCommand("commandpaneledit").setExecutor(new cpIngameEditCommand(this));
        this.getServer().getPluginManager().registerEvents(new utils(this), this);
        this.getServer().getPluginManager().registerEvents(new editorUtils(this), this);
        this.getServer().getPluginManager().registerEvents(new newGenUtils(this), this);
        this.getServer().getPluginManager().registerEvents(new commandpanelcustom(this), this);
        this.getServer().getPluginManager().registerEvents(new commandpanelUserInput(this), this);
        this.getServer().getPluginManager().registerEvents(new editorUserInput(this), this);
        this.getServer().getPluginManager().registerEvents(new commandpanelrefresher(this), this);
        this.config.addDefault("config.version", "3.0");
        this.config.addDefault("config.refresh-panels", "true");
        this.config.addDefault("config.refresh-delay", "4");
        this.config.addDefault("config.stop-sound", "true");
        this.config.addDefault("config.disabled-world-message", "true");
        this.config.addDefault("config.update-notifications", "true");
        this.config.addDefault("config.panel-snooper", "false");
        this.config.addDefault("config.ingame-editor", "true");
        this.config.addDefault("config.input-cancel", "cancel");
        this.config.addDefault("config.input-cancelled", "&cCancelled!");
        List<String> inputMessage = new ArrayList();
        //webhook test #1
        inputMessage.add("%cp-tag%&aEnter Input for Command");
        inputMessage.add("&cType &4%cp-args% &cto Cancel the command");
        this.config.addDefault("config.input-message", inputMessage);
        this.config.addDefault("config.format.perms", "&cNo permission.");
        this.config.addDefault("config.format.reload", "&aReloaded.");
        this.config.addDefault("config.format.nopanel", "&cPanel not found.");
        this.config.addDefault("config.format.noitem", "&cPanel doesn't have clickable item.");
        this.config.addDefault("config.format.notitem", "&cPlayer not found.");
        this.config.addDefault("config.format.error", "&cError found in config at");
        this.config.addDefault("config.format.needmoney", "&cInsufficient Funds!");
        this.config.addDefault("config.format.needitems", "&cInsufficient Items!");
        this.config.addDefault("config.format.bought", "&aSuccessfully Bought For $%cp-args%");
        this.config.addDefault("config.format.sold", "&aSuccessfully Sold For $%cp-args%");
        this.config.addDefault("config.format.signtag", "[CommandPanel]");
        this.config.addDefault("config.format.tag", "&6[&bCommandPanels&6]");
        this.config.addDefault("config.format.offline", "Offline");
        this.config.addDefault("config.format.offlineHeadValue", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmU1Mjg2YzQ3MGY2NmZmYTFhMTgzMzFjYmZmYjlhM2MyYTQ0MjRhOGM3MjU5YzQ0MzZmZDJlMzU1ODJhNTIyIn19fQ==");
        if (!this.panelsf.exists() || this.panelsf.list().length == 0) {
            try {
                FileConfiguration exampleFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(this.getResource("example.yml")));
                exampleFileConfiguration.save(new File(this.panelsf + File.separator + "example.yml"));
            } catch (IOException var11) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Could not save the example file!");
            }
        }

        this.config.options().copyDefaults(true);

        try {
            this.config.save(new File(this.getDataFolder() + File.separator + "config.yml"));
        } catch (IOException var10) {
            Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Could not save the config file!");
        }
        try {
            if (!this.config.getString("config.version").equals("3.0")) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Config version doesn't match the recommended version. You may run into issues.");
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Either remove the config and generate a new one, or restore the original version of the plugin initially being used.");
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Could not find config version! Your config may be missing some information!");
        }

        if (this.config.getString("config.update-notifications").equalsIgnoreCase("true")) {
            githubNewUpdate();
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
        if (Integer.parseInt(pconfig.getString("panels." + panels + ".rows")) < 7 && Integer.parseInt(pconfig.getString("panels." + panels + ".rows")) > 0) {
            Inventory i;
            if (onOpen != 3) {
                //use the regular inventory
                i = Bukkit.createInventory((InventoryHolder) null, Integer.parseInt(pconfig.getString("panels." + panels + ".rows")) * 9, ChatColor.translateAlternateColorCodes('&', pconfig.getString("panels." + panels + ".title")));
            } else {
                //this means it is the Editor window
                i = Bukkit.createInventory((InventoryHolder) null, Integer.parseInt(pconfig.getString("panels." + panels + ".rows")) * 9, ChatColor.translateAlternateColorCodes('&', ChatColor.GRAY + "Editing Panel: " + pconfig.getString("panels." + panels + ".title")));
            }
            String item = "";

            String key;
            for (Iterator var6 = pconfig.getConfigurationSection("panels." + panels + ".item").getKeys(false).iterator(); var6.hasNext(); item = item + key + " ") {
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
                //onOpen needs to not be 3 so the editor won't include hasperm and noperm items
                if (onOpen != 3) {
                    if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + ".hasvalue")) {
                        //loop through possible hasvalue 1,2,3,etc
                        for (int count = 0; pconfig.getConfigurationSection("panels." + panels + ".item." + item.split("\\s")[c]).getKeys(false).size() > count; count++) {
                            if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + ".hasvalue" + count)) {
                                boolean outputValue = true;
                                //outputValue will default to true
                                if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + ".hasvalue" + count + ".output")) {
                                    //if output is true, and values match it will be this item, vice versa
                                    outputValue = pconfig.getBoolean("panels." + panels + ".item." + item.split("\\s")[c] + ".hasvalue" + count + ".output");
                                }
                                String value = pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + ".hasvalue" + count + ".value");
                                String compare = ChatColor.stripColor(papi(p,setCpPlaceholders(p,pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + ".hasvalue" + count + ".compare"))));
                                if (compare.equals(value) == outputValue) {
                                    //onOpen being 3 means it is the editor panel.. hasvalue items cannot be included to avoid item breaking
                                    section = ".hasvalue" + count;
                                    break;
                                }
                            }
                        }
                        //this will do the hasvalue without any numbers
                        boolean outputValue = true;
                        //outputValue will default to true
                        if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + ".hasvalue.output")) {
                            //if output is true, and values match it will be this item, vice versa
                            outputValue = pconfig.getBoolean("panels." + panels + ".item." + item.split("\\s")[c] + ".hasvalue.output");
                        }
                        String value = pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + ".hasvalue.value");
                        String compare = ChatColor.stripColor(papi(p,setCpPlaceholders(p,pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + ".hasvalue.compare"))));
                        if (compare.equals(value) == outputValue) {
                            //onOpen being 3 means it is the editor panel.. hasvalue items cannot be included to avoid item breaking
                            section = ".hasvalue";
                        }
                    }
                    if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + ".hasperm")) {
                        //loop through possible noperm/hasperm 1,2,3,etc
                        for (int count = 0; pconfig.getConfigurationSection("panels." + panels + ".item." + item.split("\\s")[c]).getKeys(false).size() > count; count++) {
                            if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + ".hasperm" + count)) {
                                boolean outputValue = true;
                                //outputValue will default to true
                                if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + ".hasperm" + count + ".output")) {
                                    //if output is true, and values match it will be this item, vice versa
                                    outputValue = pconfig.getBoolean("panels." + panels + ".item." + item.split("\\s")[c] + ".hasperm" + count + ".output");
                                }
                                if (p.hasPermission(pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + ".hasperm" + count + ".perm")) == outputValue) {
                                    //onOpen being 3 means it is the editor panel.. noPerm and hasPerm items cannot be included to avoid item breaking
                                    section = ".hasperm" + count;
                                    break;
                                }
                            }
                        }
                        //this will do the hasperm without any numbers
                        boolean outputValue = true;
                        //outputValue will default to true
                        if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + ".hasperm" + ".output")) {
                            //if output is true, and values match it will be this item, vice versa
                            outputValue = pconfig.getBoolean("panels." + panels + ".item." + item.split("\\s")[c] + ".hasperm" + ".output");
                        }
                        if (p.hasPermission(pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + ".hasperm.perm")) == outputValue) {
                            section = ".hasperm";
                        }
                    }
                    //This section is for animations below here: VISUAL ONLY

                    //check for if there is animations inside the items section
                    if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + section + ".animate" + animateValue)) {
                        //check for if it contains the animate that has the animvatevalue
                        if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + section + ".animate" + animateValue)) {
                            section = section + ".animate" + animateValue;
                        }
                    }
                }
                String material = pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".material");
                if (!pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".material").equalsIgnoreCase("AIR")) {
                    ItemStack s;
                    String mat;
                    String matskull;
                    String skullname;
                    //this will convert the %cp-player-online-1-find% into cps= NAME
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

                        s = new ItemStack(Material.matchMaterial(mat), 1);

                        if (!skullname.equals("no skull") && !skullname.equals("hdb") && !matskull.split("\\s")[0].equalsIgnoreCase("cpo=")) {
                            try {
                                SkullMeta meta;
                                if (matskull.split("\\s")[1].equalsIgnoreCase("self")) {
                                    //if self/own
                                    meta = (SkullMeta) s.getItemMeta();
                                    try {
                                        meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(skullname)));
                                    } catch (Exception var23) {
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + this.config.getString("config.format.error") + " material: cps= self"));
                                        debug(var23);
                                    }
                                    s.setItemMeta(meta);
                                } else {
                                    //custom data
                                    s = this.getItem(matskull.split("\\s")[1]);
                                }
                            } catch (Exception var32) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + this.config.getString("config.format.error") + " head material: Could not load skull"));
                                debug(var32);
                            }
                        }
                        if (!skullname.equals("no skull") && matskull.split("\\s")[0].equalsIgnoreCase("cpo=")) {
                            SkullMeta cpoMeta = (SkullMeta) s.getItemMeta();
                            cpoMeta.setOwningPlayer(Bukkit.getOfflinePlayer(Bukkit.getPlayer(matskull.split("\\s")[1]).getUniqueId()));
                            s.setItemMeta(cpoMeta);
                        }

                        if (skullname.equals("hdb")) {
                            if (this.getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
                                HeadDatabaseAPI api;
                                api = new HeadDatabaseAPI();

                                try {
                                    s = api.getItemHead(matskull.split("\\s")[1].trim());
                                } catch (Exception var22) {
                                    p.sendMessage(this.papi(p, ChatColor.translateAlternateColorCodes('&', tag + this.config.getString("config.format.error") + " hdb: could not load skull!")));
                                    debug(var22);
                                }
                            } else {
                                p.sendMessage(this.papi(p, ChatColor.translateAlternateColorCodes('&', tag + "Download HeadDatabaseHook from Spigot to use this feature!")));
                            }
                        }

                        if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + section + ".enchanted")) {
                            try {
                                ItemMeta EnchantMeta;
                                if (pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".enchanted").trim().equalsIgnoreCase("true")) {
                                    EnchantMeta = s.getItemMeta();
                                    EnchantMeta.addEnchant(Enchantment.KNOCKBACK, 1, false);
                                    EnchantMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                    s.setItemMeta(EnchantMeta);
                                } else if (!pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".enchanted").trim().equalsIgnoreCase("false")) {
                                    EnchantMeta = s.getItemMeta();
                                    EnchantMeta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".enchanted").split("\\s")[0].toLowerCase())), Integer.parseInt(pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".enchanted").split("\\s")[1]), true);
                                    s.setItemMeta(EnchantMeta);
                                }
                            } catch (Exception ench) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, this.config.getString("config.format.error") + " enchanted: " + pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".enchanted"))));
                                debug(ench);
                            }
                        }
                        if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + section + ".customdata")) {
                            ItemMeta customMeta = s.getItemMeta();
                            customMeta.setCustomModelData(Integer.parseInt(pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".customdata")));
                            s.setItemMeta(customMeta);
                        }
                        if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + section + ".leatherarmor")) {
                            //if the item is leather armor, change the colour to this
                            try {
                                if (s.getType() == Material.LEATHER_BOOTS || s.getType() == Material.LEATHER_LEGGINGS || s.getType() == Material.LEATHER_CHESTPLATE || s.getType() == Material.LEATHER_HELMET) {
                                    LeatherArmorMeta leatherMeta = (LeatherArmorMeta) s.getItemMeta();
                                    String colourCode = pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".leatherarmor");
                                    if (!colourCode.contains(",")) {
                                        //use a color name
                                        leatherMeta.setColor(colourCodes.get(colourCode.toUpperCase()));
                                    } else {
                                        //use RGB sequence
                                        int[] colorRGB = {255, 255, 255};
                                        int count = 0;
                                        for (String colourNum : colourCode.split(",")) {
                                            colorRGB[count] = Integer.parseInt(colourNum);
                                            count += 1;
                                        }
                                        leatherMeta.setColor(Color.fromRGB(colorRGB[0], colorRGB[1], colorRGB[2]));
                                    }
                                    s.setItemMeta(leatherMeta);
                                }
                            } catch (Exception er) {
                                //don't colour the armor
                                debug(er);
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, this.config.getString("config.format.error") + " leatherarmor: " + pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".leatherarmor"))));
                            }
                        }
                        if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + section + ".potion")) {
                            //if the item is a potion, give it an effect
                            try {
                                PotionMeta potionMeta = (PotionMeta)s.getItemMeta();
                                String effectType = pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".potion");
                                potionMeta.setBasePotionData(new PotionData(PotionType.valueOf(effectType.toUpperCase())));
                                potionMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_POTION_EFFECTS});
                                s.setItemMeta(potionMeta);
                            } catch (Exception er) {
                                //don't add the effect
                                debug(er);
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + this.config.getString("config.format.error") + " potion: " + pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".potion")));
                            }
                        }
                        if (pconfig.contains("panels." + panels + ".item." + item.split("\\s")[c] + section + ".stack")) {
                            //change the stack amount
                            s.setAmount(Integer.parseInt(pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".stack")));
                        }
                    } catch (IllegalArgumentException var33) {
                        debug(var33);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, this.config.getString("config.format.error") + " material: " + pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".material"))));
                        return null;
                    } catch (NullPointerException var34) {
                        debug(var34);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, this.config.getString("config.format.error") + " material: " + pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".material"))));
                        return null;
                    }
                    if (onOpen != 3) {
                        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                            this.setName(s, PlaceholderAPI.setPlaceholders(p, pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".name")), PlaceholderAPI.setPlaceholders(p, (List<String>) pconfig.getList("panels." + panels + ".item." + item.split("\\s")[c] + section + ".lore")), p, true);
                        } else {
                            this.setName(s, pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".name"), pconfig.getList("panels." + panels + ".item." + item.split("\\s")[c] + section + ".lore"), p, true);
                        }
                    }else{
                        this.setName(s, pconfig.getString("panels." + panels + ".item." + item.split("\\s")[c] + section + ".name"), pconfig.getList("panels." + panels + ".item." + item.split("\\s")[c] + section + ".lore"), p, false);
                    }

                    try {
                        i.setItem(Integer.parseInt(item.split("\\s")[c]), s);
                    } catch (ArrayIndexOutOfBoundsException var24) {
                        debug(var24);
                        if (debug) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, this.config.getString("config.format.error") + " item: One of the items does not fit in the Panel!")));
                        }
                    }
                }
            }
            if (pconfig.contains("panels." + panels + ".empty") && !pconfig.getString("panels." + panels + ".empty").equals("AIR")) {
                for (c = 0; Integer.parseInt(pconfig.getString("panels." + panels + ".rows")) * 9 - 1 >= c; ++c) {
                    boolean found = false;
                    if(!item.equals("")) {
                        for (int f = 0; item.split("\\s").length - 1 >= f; ++f) {
                            if (Integer.parseInt(item.split("\\s")[f]) == c) {
                                found = true;
                            }
                        }
                    }else{
                        found = false;
                    }
                    if (!found) {
                        ItemStack empty;
                        try {
                            empty = new ItemStack(Material.matchMaterial(pconfig.getString("panels." + panels + ".empty").toUpperCase()), 1);
                            if (empty.getType() == Material.AIR) {
                                continue;
                            }
                        } catch (IllegalArgumentException var26) {
                            debug(var26);
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, this.config.getString("config.format.error") + " empty: " + pconfig.getString("panels." + panels + ".empty"))));
                            return null;
                        } catch (NullPointerException var27) {
                            debug(var27);
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, this.config.getString("config.format.error") + " empty: " + pconfig.getString("panels." + panels + ".empty"))));
                            return null;
                        }

                        ItemMeta renamedMeta = empty.getItemMeta();
                        renamedMeta.setDisplayName(" ");
                        empty.setItemMeta(renamedMeta);
                        if (onOpen != 3) {
                            //only place empty items if not editing
                            i.setItem(c, empty);
                        }
                    }
                }
            }
            if (ChatColor.translateAlternateColorCodes('&', pconfig.getString("panels." + panels + ".title")).equals("Chest")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, this.config.getString("config.format.error") + " Title: Cannot be named Chest")));
                return null;
            }
            if (ChatColor.translateAlternateColorCodes('&', pconfig.getString("panels." + panels + ".title")).contains("Editing Panel:")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, this.config.getString("config.format.error") + " Title: Cannot contain Editing Panel:")));
                return null;
            }
            if (ChatColor.translateAlternateColorCodes('&', pconfig.getString("panels." + panels + ".title")).contains("Panel Settings:")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, this.config.getString("config.format.error") + " Title: Cannot contain Panel Settings:")));
                return null;
            }
            if (ChatColor.translateAlternateColorCodes('&', pconfig.getString("panels." + panels + ".title")).contains("Item Settings:")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, this.config.getString("config.format.error") + " Title: Cannot contain Item Settings:")));
                return null;
            }
            if (ChatColor.translateAlternateColorCodes('&', pconfig.getString("panels." + panels + ".title")).equals("Command Panels Editor")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, this.config.getString("config.format.error") + " Title: Cannot be named Command Panels Editor")));
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
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, this.config.getString("config.format.error") + " rows: " + pconfig.getString("panels." + panels + ".rows"))));
            return null;
        }
    }

    public void setName(ItemStack renamed, String customName, List lore, Player p, Boolean usePlaceholders) {
        try {
            ItemMeta renamedMeta = renamed.getItemMeta();
            //set cp placeholders
            if(usePlaceholders) {
                customName = papi(p, setCpPlaceholders(p, customName));
            }

            renamedMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
            if (customName != null) {
                renamedMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
            }

            List<String> clore = new ArrayList();
            if (lore != null) {
                for (int i = 0; lore.size() > i; ++i) {
                    clore.add(ChatColor.translateAlternateColorCodes('&', lore.get(i).toString()));
                    if(usePlaceholders) {
                        clore.set(i, papi(p, setCpPlaceholders(p, clore.get(i))));
                    }
                }
                renamedMeta.setLore(clore);
            }
            renamed.setItemMeta(renamedMeta);
        } catch (Exception var11) {
        }

    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        } else {
            RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            } else {
                this.econ = (Economy) rsp.getProvider();
                return this.econ != null;
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
        GameProfile profile = new GameProfile(UUID.randomUUID(), (String) null);
        PropertyMap propertyMap = profile.getProperties();
        if (propertyMap == null) {
            throw new IllegalStateException("Profile doesn't contain a property map");
        } else {
            propertyMap.put("textures", new Property("textures", b64stringtexture));
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            ItemMeta headMeta = head.getItemMeta();
            Class headMetaClass = headMeta.getClass();

            try {
                getField(headMetaClass, "profile", GameProfile.class, 0).set(headMeta, profile);
            } catch (IllegalArgumentException var10) {
                var10.printStackTrace();
            } catch (IllegalAccessException var11) {
                var11.printStackTrace();
            }

            head.setItemMeta(headMeta);
            return head;
        }
    }

    private <T> Field getField(Class<?> target, String name, Class<T> fieldType, int index) {
        Field[] var4 = target.getDeclaredFields();
        int var5 = var4.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            Field field = var4[var6];
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

    public String papi(Player p, String setpapi) {
        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            setpapi = PlaceholderAPI.setPlaceholders(p, setpapi);
        }
        return ChatColor.translateAlternateColorCodes('&',setpapi);
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
            player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
        } else if (command.split("\\s")[0].equalsIgnoreCase("op=")) {
            //if player uses op= it will perform command as op
            boolean isop = p.isOp();
            try {
                p.setOp(true);
                if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    Bukkit.dispatchCommand(p, ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(p, command.replace("op=", "").trim())));
                } else {
                    Bukkit.dispatchCommand(p, ChatColor.translateAlternateColorCodes('&', command.replace("op=", "").trim()));
                }
                p.setOp(isop);
            } catch (Exception exc) {
                p.setOp(isop);
                debug(exc);
                if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + PlaceholderAPI.setPlaceholders(p, config.getString("config.format.error") + " op=: Error in op command!")));
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.error") + " op=: Error in op command!"));
                }
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("console=")) {
            //if player uses console= it will perform command in the console
            if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(p, command.replace("console=", "").trim())));
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', command.replace("console=", "").trim()));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("buy=")) {
            //if player uses buy= it will be eg. buy= <price> <item> <amount of item> <ID>
            try {
                if (econ != null) {
                    if (econ.getBalance(p) >= Double.parseDouble(command.split("\\s")[1])) {
                        econ.withdrawPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.bought").replaceAll("%cp-args%", command.split("\\s")[1])));
                        if (p.getInventory().firstEmpty() >= 0) {
                            p.getInventory().addItem(new ItemStack(Material.matchMaterial(command.split("\\s")[2]), Integer.parseInt(command.split("\\s")[3])));
                        } else {
                            p.getLocation().getWorld().dropItemNaturally(p.getLocation(), new ItemStack(Material.matchMaterial(command.split("\\s")[2]), Integer.parseInt(command.split("\\s")[3])));
                        }
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.needmoney")));
                    }
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Buying Requires Vault and an Economy to work!"));
                }
            } catch (Exception buy) {
                debug(buy);
                if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + PlaceholderAPI.setPlaceholders(p, config.getString("config.format.error") + " " + "commands: " + command)));
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.error") + " " + "commands: " + command));
                }
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("tokenbuy=")) {
            //if player uses tokenbuy= it will be eg. tokenbuy= <price> <item> <amount of item> <ID>
            try {
                if (this.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    int balance = Integer.parseInt(Long.toString(api.getTokens(p).orElse(0)));
                    if (balance >= Double.parseDouble(command.split("\\s")[1])) {
                        api.removeTokens(p, Long.parseLong(command.split("\\s")[1]));
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.bought").replaceAll("%cp-args%", command.split("\\s")[1])));
                        if (p.getInventory().firstEmpty() >= 0) {
                            p.getInventory().addItem(new ItemStack(Material.matchMaterial(command.split("\\s")[2]), Integer.parseInt(command.split("\\s")[3])));
                        } else {
                            p.getLocation().getWorld().dropItemNaturally(p.getLocation(), new ItemStack(Material.matchMaterial(command.split("\\s")[2]), Integer.parseInt(command.split("\\s")[3])));
                        }
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.needmoney")));
                    }
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Buying Requires TokenManager to work!"));
                }
            } catch (Exception buy) {
                debug(buy);
                if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + PlaceholderAPI.setPlaceholders(p, config.getString("config.format.error") + " " + "commands: " + command)));
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.error") + " " + "commands: " + command));
                }
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
                                    if (!potionMeta.getBasePotionData().getType().name().equalsIgnoreCase(potion)) {
                                        p.sendMessage(papi(p, tag + ChatColor.RED + "Your item has the wrong potion effect"));
                                        return;
                                    }
                                }
                            }catch(Exception exc){
                                //skip unless debug enabled
                                debug(exc);
                            }
                            if (itm.getAmount() >= new ItemStack(Material.matchMaterial(command.split("\\s")[2]), Integer.parseInt(command.split("\\s")[3])).getAmount()) {
                                int amt = itm.getAmount() - new ItemStack(Material.matchMaterial(command.split("\\s")[2]), Integer.parseInt(command.split("\\s")[3])).getAmount();
                                itm.setAmount(amt);
                                p.getInventory().setItem(f, amt > 0 ? itm : null);
                                econ.depositPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                                sold = true;
                                p.updateInventory();
                                break;
                            }
                        }
                    }
                    if (sold == false) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.needitems")));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.sold").replaceAll("%cp-args%", command.split("\\s")[1])));
                    }
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Selling Requires Vault and an Economy to work!"));
                }
            } catch (Exception sell) {
                debug(sell);
                if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + PlaceholderAPI.setPlaceholders(p, config.getString("config.format.error") + " " + "commands: " + command)));
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.error") + " " + "commands: " + command));
                }
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
                                    if (!potionMeta.getBasePotionData().getType().name().equalsIgnoreCase(potion)) {
                                        p.sendMessage(papi(p, tag + ChatColor.RED + "Your item has the wrong potion effect"));
                                        return;
                                    }
                                }
                            }catch(Exception exc){
                                //skip if it cannot do unless debug is enabled
                                debug(exc);
                            }
                            if (itm.getAmount() >= new ItemStack(Material.matchMaterial(command.split("\\s")[2]), Integer.parseInt(command.split("\\s")[3])).getAmount()) {
                                int amt = itm.getAmount() - new ItemStack(Material.matchMaterial(command.split("\\s")[2]), Integer.parseInt(command.split("\\s")[3])).getAmount();
                                itm.setAmount(amt);
                                p.getInventory().setItem(f, amt > 0 ? itm : null);
                                econ.depositPlayer(p, Double.parseDouble(command.split("\\s")[1]));
                                api.addTokens(p, Long.parseLong(command.split("\\s")[1]));
                                sold = true;
                                p.updateInventory();
                                break;
                            }
                        }
                    }
                    if (sold == false) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.needitems")));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.sold").replaceAll("%cp-args%", command.split("\\s")[1])));
                    }
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Selling Requires TokenManager to work!"));
                }
            } catch (Exception sell) {
                debug(sell);
                if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + PlaceholderAPI.setPlaceholders(p, config.getString("config.format.error") + " " + "commands: " + command)));
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.error") + " " + "commands: " + command));
                }
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("msg=")) {
            //if player uses msg= it will send the player a message
            if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(p, command.replace("msg=", "").trim())));
            } else {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', command.replace("msg=", "").trim()));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("sound=")) {
            //if player uses sound= it will play a sound (sound= [sound])
            try {
                p.playSound(p.getLocation(), Sound.valueOf(command.split("\\s")[1]), 1F, 1F);
            } catch (Exception s) {
                debug(s);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, config.getString("config.format.error") + " " + "commands: " + command)));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("tokenbuycommand=")) {
            //if player uses tokenbuycommand [price] [command]
            try {
                if (this.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    int balance = Integer.parseInt(Long.toString(api.getTokens(p).orElse(0)));
                    if (balance >= Double.parseDouble(command.split("\\s")[1])) {
                        api.removeTokens(p, Long.parseLong(command.split("\\s")[1]));
                        //execute command under here
                        String commandp = command;
                        commandp = commandp.replace("tokenbuycommand=", "").trim();
                        String price = commandp.split(" ", 2)[0];
                        commandp = commandp.split(" ", 2)[1];
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', papi(p, commandp)));
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.bought").replaceAll("%cp-args%", price)));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.needmoney")));
                    }
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Buying Requires Vault and an Economy to work!"));
                }
            } catch (Exception buyc) {
                debug(buyc);
                if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + PlaceholderAPI.setPlaceholders(p, config.getString("config.format.error") + " " + "commands: " + command)));
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.error") + " " + "commands: " + command));
                }
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
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', papi(p, commandp)));
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.bought").replaceAll("%cp-args%", price)));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.needmoney")));
                    }
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Buying Requires Vault and an Economy to work!"));
                }
            } catch (Exception buyc) {
                debug(buyc);
                if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + PlaceholderAPI.setPlaceholders(p, config.getString("config.format.error") + " " + "commands: " + command)));
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.error") + " " + "commands: " + command));
                }
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
                    otherplayer.teleport(new Location(otherplayer.getWorld(), x, y, z));
                } catch (Exception tpe) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.notitem")));
                }
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("stopsound=")) {
            //if player uses stopsound= [sound]
            try {
                p.stopSound(Sound.valueOf(command.split("\\s")[1]));
            } catch (Exception ss) {
                debug(ss);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, config.getString("config.format.error") + " " + "commands: " + command)));
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("sudo=")) {
            //if player uses sudo= [command]
            p.chat(ChatColor.translateAlternateColorCodes('&', papi(p, "/" + command.replaceAll("sudo=", "").trim())));
        } else {
            Bukkit.dispatchCommand(p, ChatColor.translateAlternateColorCodes('&', papi(p, command)));
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
        if (str.contains("%cp-player-online-")) {
            int start = str.indexOf("%cp-player-online-");
            int end = str.lastIndexOf("-find%");
            String playerLocation = str.substring(start, end).replace("%cp-player-online-", "");
            Player[] playerFind = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
            if (Integer.parseInt(playerLocation) > playerFind.length) {
                str = str.replace(str.substring(start, end) + "-find%", papi(p, ChatColor.translateAlternateColorCodes('&', config.getString("config.format.offline"))));
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
                temp = temp.replaceAll("%cp-args%", config.getString("config.input-cancel"));
                temp = temp.replaceAll("%cp-tag%", tag);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', papi(p, temp)));
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
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.bought").replaceAll("%cp-args%", command.split("\\s")[1])));
                        return 1;
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.needmoney")));
                        return 0;
                    }
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Paying Requires Vault and an Economy to work!"));
                    return 0;
                }
            } catch (Exception buyc) {
                debug(buyc);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, config.getString("config.format.error") + " " + "commands: " + command)));
                return 0;
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("tokenpaywall=")) {
            //if player uses tokenpaywall= [price]
            try {
                if (getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    int balance = Integer.parseInt(Long.toString(api.getTokens(p).orElse(0)));
                    if (balance >= Double.parseDouble(command.split("\\s")[1])) {
                        api.removeTokens(p, Long.parseLong(command.split("\\s")[1]));
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.bought").replaceAll("%cp-args%", command.split("\\s")[1])));
                        return 1;
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.needmoney")));
                        return 0;
                    }
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Paying TokenManager to work!"));
                    return 0;
                }
            } catch (Exception buyc) {
                debug(buyc);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, config.getString("config.format.error") + " " + "commands: " + command)));
                return 0;
            }
        } else if (command.split("\\s")[0].equalsIgnoreCase("xp-paywall=")) {
            //if player uses xp-paywall= [price]
            try {
                int balance = p.getLevel();
                if (balance >= Integer.parseInt(command.split("\\s")[1])) {
                    p.setLevel(p.getLevel() - Integer.parseInt(command.split("\\s")[1]));
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.bought").replaceAll("%cp-args%", command.split("\\s")[1])));
                    return 1;
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + config.getString("config.format.needmoney")));
                    return 0;
                }
            } catch (Exception buyc) {
                debug(buyc);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + papi(p, config.getString("config.format.error") + " " + "commands: " + command)));
                return 0;
            }
        } else {
            return 2;
        }
    }

    public void reloadPanelFiles() {
        try {
            panelFiles.clear();
            panelNames.clear();
            int count = 0;
            for (String fileName : Arrays.asList(panelsf.list())) {
                panelFiles.add(YamlConfiguration.loadConfiguration(new File(panelsf + File.separator + fileName)));
                for (String tempName : panelFiles.get(count).getConfigurationSection("panels").getKeys(false)) {
                    panelNames.add(new String[]{tempName, Integer.toString(count)});
                }
                count += 1;
            }
        } catch (NullPointerException noPanels) {
            this.getServer().getConsoleSender().sendMessage("[CommandPanels] No panels found to load!");
        }
    }

    public void debug(Exception e) {
        if (debug) {
            e.printStackTrace();
        }
    }

    public void helpMessage(CommandSender p) {
        String tag = config.getString("config.format.tag") + " ";
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Commands:"));
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
        reloadPanelFiles();
        Inventory i = Bukkit.createInventory((InventoryHolder) null, 54, "Command Panels Editor");
        ArrayList<String> panelNames = new ArrayList<String>(); //all panels from ALL files (panel names)
        ArrayList<String> panelTitles = new ArrayList<String>(); //all panels from ALL files (panel titles)
        ArrayList<Material> panelItems = new ArrayList<Material>(); //all panels from ALL files (panel materials)
        try {
            for (YamlConfiguration temp : panelFiles) { //will loop through all the files in folder
                String key;
                if (!checkPanels(temp)) {
                    return;
                }
                for (Iterator var10 = temp.getConfigurationSection("panels").getKeys(false).iterator(); var10.hasNext(); ) {
                    key = (String) var10.next();
                    panelNames.add(ChatColor.translateAlternateColorCodes('&', key));
                    panelTitles.add(ChatColor.translateAlternateColorCodes('&', temp.getString("panels." + key + ".title")));
                    if (temp.contains("panels." + key + ".open-with-item.material")) {
                        panelItems.add(Material.matchMaterial(temp.getString("panels." + key + ".open-with-item.material")));
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
            pageNumber = Integer.parseInt(ChatColor.stripColor(p.getOpenInventory().getItem(49).getItemMeta().getDisplayName()).replace("Page ", ""));
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
        setName(temp, ChatColor.WHITE + "Page " + pageNumber, null, p, true);
        i.setItem(49, temp);
        temp = new ItemStack(Material.BARRIER, 1);
        setName(temp, ChatColor.RED + "Exit Menu", null, p, true);
        i.setItem(45, temp);
        temp = new ItemStack(Material.BOOK, 1);
        List<String> lore = new ArrayList();
        lore.add(ChatColor.GRAY + "- Click on a panel to edit items.");
        lore.add(ChatColor.GRAY + "- Right click on a panel to edit settings.");
        lore.add(ChatColor.GRAY + "- To edit an item in a panel, shift click");
        lore.add(ChatColor.GRAY + "  on the item of choice.");
        setName(temp, ChatColor.WHITE + "Panel Editor", lore, p, true);
        i.setItem(53, temp);
        if (pageNumber != 1) {
            //only show previous page button if number is not one
            temp = new ItemStack(Material.PAPER, 1);
            setName(temp, ChatColor.WHITE + "Previous Page", null, p, true);
            i.setItem(48, temp);
        }
        if (pageNumber < pagesAmount) {
            //if page number is under pages amount
            temp = new ItemStack(Material.PAPER, 1);
            setName(temp, ChatColor.WHITE + "Next Page", null, p, true);
            i.setItem(50, temp);
        }
        int count = 0;
        int slot = 0;
        for (String panelName : panelNames) {
            //count is +1 because count starts at 0 not 1
            if ((pageNumber * 45 - 45) < (count + 1) && (pageNumber * 45) > (count)) {
                temp = new ItemStack(panelItems.get(count), 1);
                setName(temp, ChatColor.WHITE + panelName, null, p, true);
                i.setItem(slot, temp);
                slot += 1;
            }
            count += 1;
        }
        p.openInventory(i);
    }

    public void openPanelSettings(Player p, String panelName, YamlConfiguration cf) {
        reloadPanelFiles();
        Inventory i = Bukkit.createInventory((InventoryHolder) null, 27, "Panel Settings: " + panelName);
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
        lore.clear();
        lore.add(ChatColor.GRAY + "Permission required to open panel");
        lore.add(ChatColor.GRAY + "commandpanel.panel.[insert]");
        if (cf.contains("panels." + panelName + ".perm")) {
            lore.add(ChatColor.WHITE + "--------------------------------");
            lore.add(ChatColor.WHITE + "commandpanel.panel." + cf.getString("panels." + panelName + ".perm"));
        }
        setName(temp, ChatColor.WHITE + "Panel Permission", lore, p,true);
        i.setItem(1, temp);

        temp = new ItemStack(Material.NAME_TAG, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Title of the Panel");
        if (cf.contains("panels." + panelName + ".title")) {
            lore.add(ChatColor.WHITE + "------------------");
            lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".title"));
        }
        setName(temp, ChatColor.WHITE + "Panel Title", lore, p,true);
        i.setItem(3, temp);

        temp = new ItemStack(Material.JUKEBOX, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Sound when opening panel");
        if (cf.contains("panels." + panelName + ".sound-on-open")) {
            lore.add(ChatColor.WHITE + "------------------------");
            lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".sound-on-open").toUpperCase());
        }
        setName(temp, ChatColor.WHITE + "Panel Sound", lore, p,true);
        i.setItem(5, temp);

        temp = new ItemStack(Material.IRON_DOOR, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Custom command to open panel");
        if (cf.contains("panels." + panelName + ".command")) {
            lore.add(ChatColor.WHITE + "----------------------------");
            lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".command"));
        }
        setName(temp, ChatColor.WHITE + "Panel Command", lore, p,true);
        i.setItem(7, temp);

        temp = new ItemStack(Material.LAVA_BUCKET, 1);
        lore.clear();
        lore.add(ChatColor.DARK_RED + "Permanently delete Panel");
        setName(temp, ChatColor.RED + "Delete Panel", lore, p,true);
        i.setItem(21, temp);

        temp = new ItemStack(Material.PISTON, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "How many rows the panel will be");
        lore.add(ChatColor.GRAY + "choose an integer from 1 to 6");
        setName(temp, ChatColor.WHITE + "Panel Rows", lore, p,true);
        i.setItem(23, temp);

        temp = new ItemStack(Material.BLACK_STAINED_GLASS, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Fill empty slots with an item");
        if (cf.contains("panels." + panelName + ".empty")) {
            lore.add(ChatColor.WHITE + "-----------------------");
            lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".empty").toUpperCase());
        }
        setName(temp, ChatColor.WHITE + "Panel Empty Item", lore, p,true);
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
        setName(temp, ChatColor.WHITE + "Panel Commands", lore, p,true);
        i.setItem(15, temp);

        temp = new ItemStack(Material.ITEM_FRAME, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Code name to open panel");
        lore.add(ChatColor.GRAY + "/cp [name]");
        lore.add(ChatColor.WHITE + "-----------------------");
        lore.add(ChatColor.WHITE + panelName);
        setName(temp, ChatColor.WHITE + "Panel Name", lore, p,true);
        i.setItem(11, temp);

        temp = new ItemStack(Material.BARRIER, 1);
        setName(temp, ChatColor.RED + "Back", null, p,true);
        i.setItem(18, temp);

        p.openInventory(i);
    }

    public void openItemSettings(Player p, String panelName, YamlConfiguration cf, int itemNumber) {
        reloadPanelFiles();
        Inventory i = Bukkit.createInventory((InventoryHolder) null, 36, "Item Settings: " + panelName);
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
        lore.clear();
        lore.add(ChatColor.GRAY + "Display name of the item in the Panel");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".name")) {
            if (!cf.getString("panels." + panelName + ".item." + itemNumber + ".name").equals("")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".name"));
            }
        }
        setName(temp, ChatColor.WHITE + "Item Name", lore, p,true);
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
        setName(temp, ChatColor.WHITE + "Item Commands", lore, p,true);
        i.setItem(3, temp);

        temp = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Display enchantment of the item in the Panel");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".enchanted")) {
            if (!cf.getString("panels." + panelName + ".item." + itemNumber + ".name").equals("")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".enchanted"));
            }
        } else {
            lore.add(ChatColor.WHITE + "--------------------------------");
            lore.add(ChatColor.WHITE + "false");
        }
        setName(temp, ChatColor.WHITE + "Item Enchantment", lore, p,true);
        i.setItem(5, temp);

        temp = new ItemStack(Material.POTION, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Display potion effect of the item in the Panel");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".potion")) {
            if (!cf.getString("panels." + panelName + ".item." + itemNumber + ".potion").equals("")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".potion"));
            }
        }
        setName(temp, ChatColor.WHITE + "Item Potion Effect", lore, p,true);
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
        setName(temp, ChatColor.WHITE + "Item Lores", lore, p, true);
        i.setItem(19, temp);

        temp = new ItemStack(Material.ITEM_FRAME, 2);
        lore.clear();
        lore.add(ChatColor.GRAY + "How many of the item will be stacked");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".stack")) {
            if (!cf.getString("panels." + panelName + ".item." + itemNumber + ".stack").equals("")) {
                try {
                    temp.setAmount(Integer.parseInt(cf.getString("panels." + panelName + ".item." + itemNumber + ".stack")));
                } catch (Exception ex) {
                }
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".stack"));
            }
        }
        setName(temp, ChatColor.WHITE + "Item Stack Size", lore, p, true);
        i.setItem(21, temp);

        temp = new ItemStack(Material.ANVIL, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Add Custom Model Data here");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".customdata")) {
            if (!cf.getString("panels." + panelName + ".item." + itemNumber + ".customdata").equals("")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".customdata"));
            }
        }
        setName(temp, ChatColor.WHITE + "Custom Model Data", lore, p, true);
        i.setItem(23, temp);

        temp = new ItemStack(Material.LEATHER_HELMET, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Choose a colour for the armor");
        lore.add(ChatColor.GRAY + "use r,g,b or a spigot API color");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".leatherarmor")) {
            if (!cf.getString("panels." + panelName + ".item." + itemNumber + ".leatherarmor").equals("")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".leatherarmor"));
            }
        }
        setName(temp, ChatColor.WHITE + "Leather Armor Colour", lore, p, true);
        i.setItem(25, temp);

        temp = new ItemStack(Material.BARRIER, 1);
        setName(temp, ChatColor.RED + "Back", null, p, true);
        i.setItem(27, temp);

        if(cf.getString("panels." + panelName + ".item." + itemNumber + ".material").startsWith("cps=")){
            temp = new ItemStack(Material.PLAYER_HEAD, 1);
            if(cf.getString("panels." + panelName + ".item." + itemNumber + ".material").equalsIgnoreCase("cps= self")){
                //if self
                SkullMeta meta = (SkullMeta) temp.getItemMeta();
                try {
                    meta.setOwningPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()));
                } catch (Exception var23) {
                    debug(var23);
                }
                temp.setItemMeta(meta);
            }else{
                //custom head
                temp = this.getItem(cf.getString("panels." + panelName + ".item." + itemNumber + ".material").replace("cps=", "").trim());
            }
        }else if (cf.getString("panels." + panelName + ".item." + itemNumber + ".material").startsWith("%cp-player-online-")){
            //leave default for the find material tag
            temp = new ItemStack(Material.PLAYER_HEAD, 1);
        }else if (cf.getString("panels." + panelName + ".item." + itemNumber + ".material").startsWith("hdb=")){
            //head database head
            temp = new ItemStack(Material.PLAYER_HEAD, 1);
            if (this.getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
                HeadDatabaseAPI api;
                api = new HeadDatabaseAPI();
                try {
                    temp = api.getItemHead(cf.getString("panels." + panelName + ".item." + itemNumber + ".material").replace("hdb=", "").trim());
                } catch (Exception var22) {
                    debug(var22);
                }
            }
        }else{
            temp = new ItemStack(Material.matchMaterial(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")), 1);
        }
        try {
            temp.setAmount(Integer.parseInt(cf.getString("panels." + panelName + ".item." + itemNumber + ".stack")));
        } catch (Exception ex) {
            //skip
        }
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".enchanted")) {
            if (!cf.getString("panels." + panelName + ".item." + itemNumber + ".enchanted").equals("false")) {
                ItemMeta EnchantMeta;
                EnchantMeta = temp.getItemMeta();
                EnchantMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                EnchantMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                temp.setItemMeta(EnchantMeta);
            }
        }
        lore.clear();
        lore.add(ChatColor.GRAY + "Click to set custom material");
        lore.add(ChatColor.GRAY + "typically for custom heads");
        setName(temp, ChatColor.WHITE + "Item Slot " + itemNumber + " Preview", lore, p, true);
        i.setItem(35, temp);

        p.openInventory(i);
    }

    public Reader getReaderFromStream(InputStream initialStream) throws IOException {
        //this reads the encrypted resource files in the jar file
        byte[] buffer = IOUtils.toByteArray(initialStream);
        Reader targetReader = new CharSequenceReader(new String(buffer));
        return targetReader;
    }

    public void githubNewUpdate(){
        HttpURLConnection connection;
        String gitVersion;
        if(this.getDescription().getVersion().contains("-")){
            Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GREEN + " Running a custom version.");
            return;
        }
        try{
            connection = (HttpURLConnection) new URL("https://raw.githubusercontent.com/rockyhawk64/CommandPanels/master/resource/plugin.yml").openConnection();
            connection.connect();
            gitVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine().split("\\s")[1];
            if(gitVersion.contains("-")){
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Cannot check for update.");
                return;
            }
            if(this.getDescription().getVersion().equals(gitVersion)){
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GREEN + " Running the latest version.");
            }else{
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " ================================================");
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " An update for CommandPanels is available.");
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " Download version " + gitVersion + " here:");
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " https://www.spigotmc.org/resources/command-panels-custom-guis.67788/");
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " ================================================");
            }
        }catch(IOException e){
            Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Error checking for updates online.");
            debug(e);
        }
    }
}
