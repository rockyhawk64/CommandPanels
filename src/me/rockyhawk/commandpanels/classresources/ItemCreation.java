package me.rockyhawk.commandpanels.classresources;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ItemCreation {
    CommandPanels plugin;
    public ItemCreation(CommandPanels pl) {
        plugin = pl;
    }

    @SuppressWarnings("deprecation")
    public ItemStack makeItemFromConfig(ConfigurationSection itemSection, Player p, boolean placeholders, boolean colours){
        String tag = plugin.config.getString("config.format.tag") + " ";
        String material = plugin.papiNoColour(p,itemSection.getString("material"));
        try {
            if (Objects.requireNonNull(material).equalsIgnoreCase("AIR")) {
                return null;
            }
        }catch(NullPointerException e){
            plugin.debug(e);
            p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " material: could not load material!"));
            return null;
        }
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
                material = material.replace(material.substring(start, end) + "-find%", "cps= " + plugin.config.getString("config.format.offlineHeadValue"));
            } else {
                material = material.replace(material.substring(start, end) + "-find%", "cpo= " + playerFind[Integer.parseInt(playerLocation) - 1].getName());
                //cpo is to get the skull of the player online. It is fine since the plugin knows the player is online
            }
        }
        try {
            mat = material.toUpperCase();
            matskull = material;
            skullname = "no skull";
            short id = 0;
            if(itemSection.contains("ID")){
                id = Short.parseShort(itemSection.getString("ID"));
            }
            if (matskull.split("\\s")[0].toLowerCase().equals("cps=") || matskull.split("\\s")[0].toLowerCase().equals("cpo=")) {
                skullname = p.getUniqueId().toString();
                mat = plugin.getHeads.playerHeadString();
                if(plugin.legacy.isLegacy()){
                    id = 3;
                }
            }

            if (matskull.split("\\s")[0].toLowerCase().equals("hdb=")) {
                skullname = "hdb";
                mat = plugin.getHeads.playerHeadString();
                if(plugin.legacy.isLegacy()){
                    id = 3;
                }
            }

            s = new ItemStack(Objects.requireNonNull(Material.matchMaterial(mat)), 1,id);

            if (!skullname.equals("no skull") && !skullname.equals("hdb") && !matskull.split("\\s")[0].equalsIgnoreCase("cpo=")) {
                try {
                    SkullMeta meta;
                    if (matskull.split("\\s")[1].equalsIgnoreCase("self")) {
                        //if cps= self
                        meta = (SkullMeta) s.getItemMeta();
                        if(!plugin.legacy.isLegacy()) {
                            try {
                                assert meta != null;
                                meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(skullname)));
                            } catch (Exception var23) {
                                p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " material: cps= self"));
                                plugin.debug(var23);
                            }
                        }else{
                            meta.setOwner(p.getName());
                        }
                        s.setItemMeta(meta);
                    }else if (plugin.papiNoColour(p,matskull.split("\\s")[1]).length() <= 16) {
                        //if cps= username
                        s = plugin.customHeads.getPlayerHead(plugin.papiNoColour(p,matskull.split("\\s")[1]));
                    } else {
                        //custom data cps= base64
                        s = plugin.customHeads.getCustomHead(plugin.papiNoColour(p,matskull.split("\\s")[1]));
                    }
                } catch (Exception var32) {
                    p.sendMessage(plugin.papi( tag + plugin.config.getString("config.format.error") + " head material: Could not load skull"));
                    plugin.debug(var32);
                }
            }
            if (!skullname.equals("no skull") && matskull.split("\\s")[0].equalsIgnoreCase("cpo=")) {
                SkullMeta cpoMeta = (SkullMeta) s.getItemMeta();
                assert cpoMeta != null;
                cpoMeta.setOwningPlayer(Bukkit.getOfflinePlayer(Objects.requireNonNull(Bukkit.getPlayer(matskull.split("\\s")[1])).getUniqueId()));
                s.setItemMeta(cpoMeta);
            }
            if (skullname.equals("hdb")) {
                if (plugin.getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
                    HeadDatabaseAPI api;
                    api = new HeadDatabaseAPI();

                    try {
                        s = api.getItemHead(matskull.split("\\s")[1].trim());
                    } catch (Exception var22) {
                        p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " hdb: could not load skull!"));
                        plugin.debug(var22);
                    }
                } else {
                    p.sendMessage(plugin.papi(tag + "Download HeadDatabaseHook from Spigot to use this feature!"));
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
                    if(new File(plugin.getDataFolder().getPath() + File.separator + "maps" + File.separator + itemSection.getString("map")).exists()) {
                        map.addRenderer(new MapRenderer() {
                            public void render(MapView view, MapCanvas canvas, Player player) {
                                canvas.drawImage(0, 0, new ImageIcon(plugin.getDataFolder().getPath() + File.separator + "maps" + File.separator + itemSection.getString("map")).getImage());
                            }
                        });
                        MapMeta meta = (MapMeta) s.getItemMeta();
                        meta.setMapView(map);
                        s.setItemMeta(meta);
                    }else{
                        p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " map: File not found."));
                    }
                }catch(Exception map){
                    p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " map: " + itemSection.getString("map")));
                    plugin.debug(map);
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
                    p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " enchanted: " + itemSection.getString("enchanted")));
                    plugin.debug(ench);
                }
            }
            if (itemSection.contains("customdata")) {
                ItemMeta customMeta = s.getItemMeta();
                assert customMeta != null;
                customMeta.setCustomModelData(Integer.parseInt(Objects.requireNonNull(itemSection.getString("customdata"))));
                s.setItemMeta(customMeta);
            }
            try {
                if (itemSection.contains("banner")) {
                    BannerMeta bannerMeta = (BannerMeta) s.getItemMeta();
                    List<Pattern> patterns = new ArrayList<>(); //Load patterns in order top to bottom
                    for (String temp : itemSection.getStringList("banner")) {
                        String[] dyePattern = temp.split(",");
                        patterns.add(new Pattern(DyeColor.valueOf(dyePattern[0]), PatternType.valueOf(dyePattern[1]))); //load patterns in config: RED:STRIPE_TOP
                    }
                    bannerMeta.setPatterns(patterns);
                    s.setItemMeta(bannerMeta);
                }
            }catch(Exception ignore){
                //not a banner or error
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
                            leatherMeta.setColor(plugin.colourCodes.get(colourCode.toUpperCase()));
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
                    plugin.debug(er);
                    p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " leatherarmor: " + itemSection.getString("leatherarmor")));
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
                    plugin.debug(er);
                    p.sendMessage(plugin.papi(tag + ChatColor.RED + plugin.config.getString("config.format.error") + " potion: " + itemSection.getString("potion")));
                }
            }
            if (itemSection.contains("damage")) {
                //change the damage amount (placeholders accepted)
                if(plugin.legacy.isLegacy()){
                    try {
                        s.setDurability(Short.parseShort(Objects.requireNonNull(plugin.papi(p, itemSection.getString("damage")))));
                    }catch(Exception e){
                        plugin.debug(e);
                        p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " damage: " + itemSection.getString("damage")));
                    }
                }else {
                    try {
                        Damageable itemDamage = (Damageable) s.getItemMeta();
                        itemDamage.setDamage(Integer.parseInt(Objects.requireNonNull(plugin.papi(p, itemSection.getString("damage")))));
                        s.setItemMeta((ItemMeta) itemDamage);
                    } catch (Exception e) {
                        plugin.debug(e);
                        p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " damage: " + itemSection.getString("damage")));
                    }
                }
            }
            if (itemSection.contains("stack")) {
                //change the stack amount (placeholders accepted)
                s.setAmount(Integer.parseInt(Objects.requireNonNull(plugin.papi(p,itemSection.getString("stack")))));
            }
        } catch (IllegalArgumentException | NullPointerException var33) {
            plugin.debug(var33);
            p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " material: " + itemSection.getString("material")));
            return null;
        }
        plugin.setName(s, itemSection.getString("name"), itemSection.getStringList("lore"), p, placeholders, colours);
        return s;
    }

    //hasperm hasvalue, etc sections will be done here
    public String hasSection(ConfigurationSection cf, Player p){
        if (cf.contains("hasvalue")) {
            //this will do the hasvalue without any numbers
            boolean outputValue = true;
            //outputValue will default to true
            if (cf.contains("hasvalue.output")) {
                //if output is true, and values match it will be this item, vice versa
                outputValue = cf.getBoolean("hasvalue.output");
            }
            String value = ChatColor.stripColor(plugin.papi(p,plugin.setCpPlaceholders(p,cf.getString("hasvalue.value"))));
            String compare = ChatColor.stripColor(plugin.papi(p,plugin.setCpPlaceholders(p,cf.getString("hasvalue.compare"))));
            if (compare.equals(value) == outputValue) {
                //onOpen being 3 means it is the editor panel.. hasvalue items cannot be included to avoid item breaking
                String section = hasSection(Objects.requireNonNull(cf.getConfigurationSection("hasvalue")), p);
                //string section, it executes itself to check for subsections
                return ".hasvalue" + section;
            }
            //loop through possible hasvalue 1,2,3,etc
            for (int count = 0; cf.getKeys(false).size() > count; count++) {
                if (cf.contains("hasvalue" + count)) {
                    outputValue = true;
                    //outputValue will default to true
                    if (cf.contains("hasvalue" + count + ".output")) {
                        //if output is true, and values match it will be this item, vice versa
                        outputValue = cf.getBoolean("hasvalue" + count + ".output");
                    }
                    value = ChatColor.stripColor(plugin.papi(p,plugin.setCpPlaceholders(p,cf.getString("hasvalue" + count + ".value"))));
                    compare = ChatColor.stripColor(plugin.papi(p,plugin.setCpPlaceholders(p,cf.getString("hasvalue" + count + ".compare"))));
                    if (compare.equals(value) == outputValue) {
                        //onOpen being 3 means it is the editor panel.. hasvalue items cannot be included to avoid item breaking
                        String section = hasSection(Objects.requireNonNull(cf.getConfigurationSection("hasvalue" + count)), p);
                        //string section, it executes itself to check for subsections
                        return ".hasvalue" + count + section;
                    }
                }
            }
        }
        if (cf.contains("hasgreater")) {
            //this will do the hasgreater without any numbers
            boolean outputValue = true;
            //outputValue will default to true
            if (cf.contains("hasgreater.output")) {
                //if output is true, and values match it will be this item, vice versa
                outputValue = cf.getBoolean("hasgreater.output");
            }
            double value = Double.parseDouble(ChatColor.stripColor(plugin.papiNoColour(p,cf.getString("hasgreater.value"))));
            double compare = Double.parseDouble(ChatColor.stripColor(plugin.papiNoColour(p,cf.getString("hasgreater.compare"))));
            if ((compare >= value) == outputValue) {
                //onOpen being 3 means it is the editor panel.. hasgreater items cannot be included to avoid item breaking
                String section = hasSection(Objects.requireNonNull(cf.getConfigurationSection("hasgreater")), p);
                return ".hasgreater" + section;
            }
            //loop through possible hasgreater 1,2,3,etc
            for (int count = 0; cf.getKeys(false).size() > count; count++) {
                if (cf.contains("hasgreater" + count)) {
                    outputValue = true;
                    //outputValue will default to true
                    if (cf.contains("hasgreater" + count + ".output")) {
                        //if output is true, and values match it will be this item, vice versa
                        outputValue = cf.getBoolean("hasgreater" + count + ".output");
                    }
                    value = Double.parseDouble(ChatColor.stripColor(plugin.papiNoColour(p,cf.getString("hasgreater" + count + ".value"))));
                    compare = Double.parseDouble(ChatColor.stripColor(plugin.papiNoColour(p,cf.getString("hasgreater" + count + ".compare"))));
                    if ((compare >= value) == outputValue) {
                        //onOpen being 3 means it is the editor panel.. hasgreater items cannot be included to avoid item breaking
                        String section = hasSection(Objects.requireNonNull(cf.getConfigurationSection("hasgreater" + count)), p);
                        return ".hasgreater" + count + section;
                    }
                }
            }
        }
        if (cf.contains("hasperm")) {
            //this will do hasperm with no numbers
            boolean outputValue = true;
            //outputValue will default to true
            if (cf.contains("hasperm.output")) {
                //if output is true, and values match it will be this item, vice versa
                outputValue = cf.getBoolean("hasperm.output");
            }
            if (p.hasPermission(Objects.requireNonNull(cf.getString("hasperm.perm"))) == outputValue) {
                String section = hasSection(Objects.requireNonNull(cf.getConfigurationSection("hasperm")), p);
                return ".hasperm" + section;
            }
            for(int count = 0; cf.getKeys(false).size() > count; count++){
                if (cf.contains("hasperm" + count) && cf.contains("hasperm"  + count + ".perm")) {
                    outputValue = true;
                    //outputValue will default to true
                    if (cf.contains("hasperm" + count + ".output")) {
                        //if output is true, and values match it will be this item, vice versa
                        outputValue = cf.getBoolean("hasperm" + count + ".output");
                    }
                    if (p.hasPermission(Objects.requireNonNull(cf.getString("hasperm" + count + ".perm"))) == outputValue) {
                        String section = hasSection(Objects.requireNonNull(cf.getConfigurationSection("hasperm" + count)), p);
                        return ".hasperm" + count + section;
                    }
                }
            }
        }
        return "";
    }

    @SuppressWarnings("deprecation")
    public YamlConfiguration generatePanelFile(String panelName, Inventory inv, YamlConfiguration file){
        ItemStack cont;
        for(int i = 0; inv.getSize() > i; i++){
            cont = inv.getItem(i);
            //repeat through all the items in the editor
            try{
                //make the item here
                if(cont == null){
                    //remove if items have been removed
                    if(file.contains("panels." + panelName + ".item." + i)){
                        file.set("panels." + panelName + ".item." + i, null);
                        continue;
                    }
                }
                if(plugin.legacy.isLegacy()){
                    if (cont.getDurability() != 0 && !cont.getType().toString().equals("SKULL_ITEM")) {
                        file.set("panels." + panelName + ".item." + i + ".ID", cont.getDurability());
                    }
                }
                if(file.contains("panels." + panelName + ".item." + i + ".material")){
                    if(Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("%") || Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("=")){
                        if(!plugin.getHeads.ifSkullOrHead(cont.getType().toString())){
                            file.set("panels." + panelName + ".item." + i + ".material", cont.getType().toString());
                        }
                    }else{
                        file.set("panels." + panelName + ".item." + i + ".material", cont.getType().toString());
                    }
                }else{
                    file.set("panels." + panelName + ".item." + i + ".material", cont.getType().toString());
                }
                if(plugin.getHeads.ifSkullOrHead(cont.getType().toString())){
                    if(!Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("%") && !Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("=")) {
                        SkullMeta meta = (SkullMeta) cont.getItemMeta();
                        //disable for legacy as is broken
                        if (!plugin.legacy.isLegacy()) {
                            if (plugin.customHeads.getHeadBase64(cont) != null) {
                                //inject base64 here
                                file.set("panels." + panelName + ".item." + i + ".material", "cps= " + plugin.customHeads.getHeadBase64(cont));
                            } else if (meta.hasOwner()) {
                                //check for skull owner
                                file.set("panels." + panelName + ".item." + i + ".material", "cps= " + meta.getOwner());
                            }
                        }
                    }
                }
                try {
                    BannerMeta bannerMeta = (BannerMeta) cont.getItemMeta();
                    List<String> dyePattern = new ArrayList<>();
                    for(Pattern pattern : bannerMeta.getPatterns()) { //sublist to skip first value
                        dyePattern.add(pattern.getColor().toString() + "," + pattern.getPattern().toString());
                    }
                    file.set("panels." + panelName + ".item." + i + ".banner", dyePattern);
                }catch(Exception ignore){
                    //not a banner
                    file.set("panels." + panelName + ".item." + i + ".banner", null);
                }
                file.set("panels." + panelName + ".item." + i + ".stack", cont.getAmount());
                if(!cont.getEnchantments().isEmpty()){
                    file.set("panels." + panelName + ".item." + i + ".enchanted", "true");
                }
                file.set("panels." + panelName + ".item." + i + ".name", Objects.requireNonNull(cont.getItemMeta()).getDisplayName());
                file.set("panels." + panelName + ".item." + i + ".lore", Objects.requireNonNull(cont.getItemMeta()).getLore());
            }catch(Exception n){
                //skip over an item that spits an error
            }
        }
        return file;
    }
}
