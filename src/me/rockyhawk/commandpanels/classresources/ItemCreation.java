package me.rockyhawk.commandpanels.classresources;

import com.jojodmo.customitems.api.CustomItemsAPI;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.manager.ItemManager;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
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
import java.util.stream.Collectors;

public class ItemCreation {
    CommandPanels plugin;
    public ItemCreation(CommandPanels pl) {
        plugin = pl;
    }

    @SuppressWarnings("deprecation")
    public ItemStack makeItemFromConfig(Panel panel, PanelPosition position, ConfigurationSection itemSection, Player p, boolean placeholders, boolean colours, boolean addNBT){
        String material = plugin.tex.placeholdersNoColour(panel,position,p,itemSection.getString("material"));
        try {
            if (Objects.requireNonNull(material).equalsIgnoreCase("AIR")) {
                return null;
            }
        }catch(NullPointerException e){
            plugin.debug(e,p);
            p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " material: could not load material!"));
            return null;
        }
        ItemStack s = null;
        boolean hideAttributes = true;
        String mat;
        String matraw;
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
            //can be changed
            mat = material.toUpperCase();
            //cannot be changed (raw)
            matraw = material;
            //generate item stack normally
            boolean normalCreation = true;
            //name of head/skull if used
            skullname = "no skull";
            short id = 0;
            if(itemSection.contains("ID")){
                id = Short.parseShort(itemSection.getString("ID"));
            }
            if (matraw.split("\\s")[0].equalsIgnoreCase("cps=") || matraw.split("\\s")[0].toLowerCase().equals("cpo=")) {
                skullname = p.getUniqueId().toString();
                mat = plugin.getHeads.playerHeadString();
                if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)){
                    id = 3;
                }
            }

            if (matraw.split("\\s")[0].equalsIgnoreCase("hdb=")) {
                skullname = "hdb";
                mat = plugin.getHeads.playerHeadString();
                if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)){
                    id = 3;
                }
            }

            //creates custom MMOItems items
            if(matraw.split("\\s")[0].equalsIgnoreCase("mmo=") && plugin.getServer().getPluginManager().isPluginEnabled("MMOItems")){
                String itemType = matraw.split("\\s")[1];
                String itemID = matraw.split("\\s")[2];
                ItemManager itemManager = MMOItems.plugin.getItems();
                MMOItem mmoitem = itemManager.getMMOItem(MMOItems.plugin.getTypes().get(itemType), itemID);
                s = mmoitem.newBuilder().build();
                normalCreation = false;
            }

            //creates Custom Items items
            if(matraw.split("\\s")[0].equalsIgnoreCase("cui=") && plugin.getServer().getPluginManager().isPluginEnabled("CustomItems")){
                s = CustomItemsAPI.getCustomItem("cui:" + matraw.split("\\s")[1]);
                normalCreation = false;
            }

            //creates a written book item
            if(matraw.split("\\s")[0].equalsIgnoreCase("book=")){
                s = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta bookMeta = (BookMeta) s.getItemMeta();
                bookMeta.setTitle(matraw.split("\\s")[1]);
                bookMeta.setAuthor(matraw.split("\\s")[1]);
                List<String> bookLines = plugin.tex.placeholdersList(panel,position,p,itemSection.getStringList("write"),true);
                String result = bookLines.stream().map(String::valueOf).collect(Collectors.joining("\n" + ChatColor.RESET, "", ""));
                bookMeta.setPages(result);
                s.setItemMeta(bookMeta);
                normalCreation = false;
            }

            //creates item from custom-items section of panel
            if(matraw.split("\\s")[0].equalsIgnoreCase("cpi=")){
                s = makeCustomItemFromConfig(panel,position,panel.getConfig().getConfigurationSection("custom-item." + matraw.split("\\s")[1]), p, true, true, true);
                normalCreation = false;
            }

            if(normalCreation) {
                s = new ItemStack(Objects.requireNonNull(Material.matchMaterial(mat)), 1, id);
            }

            if (!skullname.equals("no skull") && !skullname.equals("hdb") && !matraw.split("\\s")[0].equalsIgnoreCase("cpo=")) {
                try {
                    SkullMeta meta;
                    if (matraw.split("\\s")[1].equalsIgnoreCase("self")) {
                        //if cps= self
                        meta = (SkullMeta) s.getItemMeta();
                        if(!plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)) {
                            try {
                                assert meta != null;
                                meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(skullname)));
                            } catch (Exception var23) {
                                p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " material: cps= self"));
                                plugin.debug(var23,p);
                            }
                        }else{
                            meta.setOwner(p.getName());
                        }
                        s.setItemMeta(meta);
                    }else if (plugin.tex.placeholdersNoColour(panel,position,p,matraw.split("\\s")[1]).length() <= 16) {
                        //if cps= username
                        s = plugin.customHeads.getPlayerHead(plugin.tex.placeholdersNoColour(panel,position,p,matraw.split("\\s")[1]));
                    } else {
                        //custom data cps= base64
                        s = plugin.customHeads.getCustomHead(plugin.tex.placeholdersNoColour(panel,position,p,matraw.split("\\s")[1]));
                    }
                } catch (Exception var32) {
                    p.sendMessage(plugin.tex.colour( plugin.tag + plugin.config.getString("config.format.error") + " head material: Could not load skull"));
                    plugin.debug(var32,p);
                }
            }
            if (!skullname.equals("no skull") && matraw.split("\\s")[0].equalsIgnoreCase("cpo=")) {
                SkullMeta cpoMeta = (SkullMeta) s.getItemMeta();
                assert cpoMeta != null;
                cpoMeta.setOwningPlayer(Bukkit.getOfflinePlayer(Objects.requireNonNull(Bukkit.getPlayer(matraw.split("\\s")[1])).getUniqueId()));
                s.setItemMeta(cpoMeta);
            }
            if (skullname.equals("hdb")) {
                if (plugin.getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
                    HeadDatabaseAPI api;
                    api = new HeadDatabaseAPI();

                    try {
                        s = api.getItemHead(matraw.split("\\s")[1].trim());
                    } catch (Exception var22) {
                        p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " hdb: could not load skull!"));
                        plugin.debug(var22,p);
                    }
                } else {
                    p.sendMessage(plugin.tex.colour(plugin.tag + "Download HeadDatabaseHook from Spigot to use this feature!"));
                }
            }

            //itemType values
            if(itemSection.contains("itemType")){
                //if hidden, reverse
                if(itemSection.getStringList("itemType").contains("noAttributes")){
                    hideAttributes = false;
                }
                if(itemSection.getStringList("itemType").contains("noNBT")){
                    addNBT = false;
                }
                if(itemSection.getStringList("itemType").contains("placeable")){
                    addNBT = false;
                }
            }

            if(addNBT){
                s = plugin.nbt.setNBT(s);
            }

            if (itemSection.contains("map")) {
                /*
                This will do maps from custom images
                the maps will be in the 'maps' folder, so
                CommandPanels/maps/image.png <-- here
                CommandPanels/panels/example_top.yml
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
                        p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " map: File not found."));
                    }
                }catch(Exception map){
                    p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " map: " + itemSection.getString("map")));
                    plugin.debug(map,p);
                }
            }
            if (itemSection.contains("enchanted")) {
                try {
                    ItemMeta EnchantMeta;
                    if(itemSection.isList("enchanted")){
                        //if there is a list of enchantments to add
                        EnchantMeta = s.getItemMeta();
                        assert EnchantMeta != null;
                        for(String enchantment : itemSection.getStringList("enchanted")){
                            EnchantMeta.addEnchant(Objects.requireNonNull(EnchantmentWrapper.getByKey(NamespacedKey.minecraft(enchantment.split("\\s")[0].toLowerCase()))), Integer.parseInt(enchantment.split("\\s")[1]), true);
                        }
                        s.setItemMeta(EnchantMeta);
                    }else if (Objects.requireNonNull(itemSection.getString("enchanted")).trim().equalsIgnoreCase("true")) {
                        //is used if enchanted is set to true
                        EnchantMeta = s.getItemMeta();
                        assert EnchantMeta != null;
                        EnchantMeta.addEnchant(Enchantment.KNOCKBACK, 1, false);
                        EnchantMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        s.setItemMeta(EnchantMeta);
                    } else if (!Objects.requireNonNull(itemSection.getString("enchanted")).trim().equalsIgnoreCase("false")) {
                        //if used to ensure enchanted does not equal false but equals something else
                        EnchantMeta = s.getItemMeta();
                        assert EnchantMeta != null;
                        EnchantMeta.addEnchant(Objects.requireNonNull(EnchantmentWrapper.getByKey(NamespacedKey.minecraft(Objects.requireNonNull(itemSection.getString("enchanted")).split("\\s")[0].toLowerCase()))), Integer.parseInt(Objects.requireNonNull(itemSection.getString("enchanted")).split("\\s")[1]), true);
                        s.setItemMeta(EnchantMeta);
                    }
                } catch (Exception ench) {
                    p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " enchanted: " + itemSection.getString("enchanted")));
                    plugin.debug(ench,p);
                }
            }
            if (itemSection.contains("customdata")) {
                ItemMeta customMeta = s.getItemMeta();
                assert customMeta != null;
                customMeta.setCustomModelData(Integer.parseInt(plugin.tex.placeholders(panel,position,p,itemSection.getString("customdata"))));
                s.setItemMeta(customMeta);
            }
            try {
                if (itemSection.contains("banner")) {
                    BannerMeta bannerMeta = (BannerMeta) s.getItemMeta();
                    List<Pattern> patterns = new ArrayList<>(); //Load patterns in order top to bottom
                    for (String temp : itemSection.getStringList("banner")) {
                        temp = plugin.tex.placeholdersNoColour(panel,position,p,temp);
                        String[] dyePattern = temp.split(",");
                        patterns.add(new Pattern(DyeColor.valueOf(dyePattern[0]), PatternType.valueOf(dyePattern[1]))); //load patterns in config: RED,STRIPE_TOP
                    }
                    bannerMeta.setPatterns(patterns);
                    bannerMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                    s.setItemMeta(bannerMeta);
                }
            }catch(Exception ignore){
                //not a banner or error
            }
            if (itemSection.contains("leatherarmor")) {
                //if the item is leather armor, change the colour to this
                try {
                    if (s.getType() == Material.LEATHER_BOOTS ||
                            s.getType() == Material.LEATHER_LEGGINGS ||
                            s.getType() == Material.LEATHER_CHESTPLATE ||
                            s.getType() == Material.LEATHER_HELMET ||
                            s.getType() == Material.matchMaterial("LEATHER_HORSE_ARMOR")) { //avoid exceptions on older versions which don't have leather armour
                        LeatherArmorMeta leatherMeta = (LeatherArmorMeta) s.getItemMeta();
                        String colourCode = plugin.tex.placeholdersNoColour(panel,position,p,itemSection.getString("leatherarmor"));
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
                    plugin.debug(er,p);
                    p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " leatherarmor: " + itemSection.getString("leatherarmor")));
                }
            }

            if (itemSection.contains("potion")) {
                //if the item is a potion, give it an effect
                try {
                    PotionMeta potionMeta = (PotionMeta)s.getItemMeta();
                    String[] effectType = plugin.tex.placeholdersNoColour(panel,position,p,itemSection.getString("potion")).split("\\s");
                    assert potionMeta != null;
                    boolean extended = false;
                    boolean upgraded = false;
                    //create data
                    if(effectType.length >= 2){
                        if(effectType[1].equalsIgnoreCase("true")){
                            extended = true;
                        }
                        if(effectType.length == 3){
                            if(effectType[2].equalsIgnoreCase("true")){
                                upgraded = true;
                            }
                        }
                    }
                    PotionData newData = new PotionData(PotionType.valueOf(effectType[0].toUpperCase()),extended,upgraded);
                    //set meta
                    potionMeta.setBasePotionData(newData);
                    potionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                    s.setItemMeta(potionMeta);
                } catch (Exception er) {
                    //don't add the effect
                    plugin.debug(er,p);
                    p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + plugin.config.getString("config.format.error") + " potion: " + itemSection.getString("potion")));
                }
            }
            if (itemSection.contains("damage")) {
                //change the damage amount (placeholders accepted)
                //if the damage is not unbreakable and should be a value
                if (plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)) {
                    try {
                        s.setDurability(Short.parseShort(Objects.requireNonNull(plugin.tex.placeholders(panel,position,p, itemSection.getString("damage")))));
                    } catch (Exception e) {
                        plugin.debug(e, p);
                        p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " damage: " + itemSection.getString("damage")));
                    }
                } else {
                    if(itemSection.getString("damage").equalsIgnoreCase("-1")){
                        //if the player wants the item to be unbreakable. Only works in non legacy versions
                        ItemMeta unbreak = s.getItemMeta();
                        unbreak.setUnbreakable(true);
                        s.setItemMeta(unbreak);
                    }

                    try {
                        Damageable itemDamage = (Damageable) s.getItemMeta();
                        itemDamage.setDamage(Integer.parseInt(Objects.requireNonNull(plugin.tex.placeholders(panel,position,p, itemSection.getString("damage")))));
                        s.setItemMeta((ItemMeta) itemDamage);
                    } catch (Exception e) {
                        plugin.debug(e, p);
                        p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " damage: " + itemSection.getString("damage")));
                    }
                }
            }
            if (itemSection.contains("nbt")) {
                for(String key : itemSection.getConfigurationSection("nbt").getKeys(false)){
                    s = plugin.nbt.setNBT(s,key,itemSection.getString("nbt." + key));
                }
            }
            if (itemSection.contains("stack")) {
                //change the stack amount (placeholders accepted)
                s.setAmount((int)Double.parseDouble(Objects.requireNonNull(plugin.tex.placeholders(panel,position,p,itemSection.getString("stack")))));
            }
            //do the items commands throughout the refresh
            //check that the panel is already open and not running commands when opening
            if (itemSection.contains("refresh-commands") && plugin.openPanels.hasPanelOpen(p.getName(), panel.getName(), position)) {
                try {
                    plugin.commandTags.runCommands(panel,position,p,itemSection.getStringList("refresh-commands"));
                }catch(Exception ex){
                    plugin.debug(ex,p);
                }
            }
        } catch (IllegalArgumentException | NullPointerException var33) {
            plugin.debug(var33,p);
            p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " material: " + itemSection.getString("material")));
            return null;
        }
        plugin.setName(panel,s, itemSection.getString("name"), itemSection.getStringList("lore"), p, placeholders, colours, hideAttributes);
        return s;
    }

    //do custom-item items, they have an additional hasSection requirement
    public ItemStack makeCustomItemFromConfig(Panel panel,PanelPosition position, ConfigurationSection itemSection, Player p, boolean placeholders, boolean colours, boolean addNBT){
        String section = plugin.has.hasSection(panel,position,itemSection,p);
        if(!section.equals("")){
            itemSection = itemSection.getConfigurationSection(section.substring(1));
        }
        return plugin.itemCreate.makeItemFromConfig(panel,position,itemSection, p, placeholders, colours, addNBT);
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
                        //if the material doesn't equal air (don't delete air materials in the editor)
                        if(!file.getString("panels." + panelName + ".item." + i + ".material").equalsIgnoreCase("AIR")) {
                            file.set("panels." + panelName + ".item." + i, null);
                            continue;
                        }
                    }
                }
                if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)){
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
                        if (plugin.customHeads.getHeadBase64(cont) != null && !plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)) {
                            //inject base64 here, disable for legacy as is not working
                            file.set("panels." + panelName + ".item." + i + ".material", "cps= " + plugin.customHeads.getHeadBase64(cont));
                        } else if (meta.hasOwner()) {
                            //check for skull owner
                            file.set("panels." + panelName + ".item." + i + ".material", "cps= " + meta.getOwner());
                        }
                    }
                }
                try {
                    BannerMeta bannerMeta = (BannerMeta) cont.getItemMeta();
                    List<String> dyePattern = new ArrayList<>();
                    for(Pattern pattern : bannerMeta.getPatterns()) { //sublist to skip first value
                        dyePattern.add(pattern.getColor() + "," + pattern.getPattern());
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

    /*
    The ItemStack 'one' will be used, if it doesn't have a lore for example, it won't check to see if the other does have one
    The isIdentical() function will check for the following
    Material, Name, Lore, Enchanted, Potion
     */
    @SuppressWarnings("deprecation")
    public boolean isIdentical(ItemStack one, ItemStack two){
        //check material
        if (one.getType() != two.getType()) {
            return false;
        }
        //check for name
        try {
            if (!one.getItemMeta().getDisplayName().equals(two.getItemMeta().getDisplayName())) {
                if(one.getItemMeta().hasDisplayName()) {
                    return false;
                }
            }
        }catch(Exception ignore){}
        //check for lore
        try {
            if (!one.getItemMeta().getLore().equals(two.getItemMeta().getLore())) {
                if(one.getItemMeta().hasLore()) {
                    return false;
                }
            }
        }catch(Exception ignore){}
        //check for damage
        try {
            if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)){
                if(one.getDurability() != two.getDurability()) {
                    return false;
                }
            }else {
                Damageable tempOne = (Damageable) one.getItemMeta();
                Damageable tempTwo = (Damageable) two.getItemMeta();
                if(tempOne.getDamage() != tempTwo.getDamage()){
                    return false;
                }
            }
        } catch (Exception ignore) {}
        //check for potions
        try {
            PotionMeta meta1 = (PotionMeta) one.getItemMeta();
            PotionMeta meta2 = (PotionMeta) two.getItemMeta();
            //different duration
            if(meta1.getBasePotionData().isExtended() != meta2.getBasePotionData().isExtended()){
                return false;
            }
            //different upgrade
            if(meta1.getBasePotionData().isUpgraded() != meta2.getBasePotionData().isUpgraded()){
                return false;
            }
            //different potion type
            if (meta1.getBasePotionData().getType().compareTo(meta2.getBasePotionData().getType()) != 0){
                return false;
            }
        }catch(Exception ignore){}
        //check for enchantments
        if(one.getEnchantments() == two.getEnchantments()){
            if(!one.getEnchantments().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
