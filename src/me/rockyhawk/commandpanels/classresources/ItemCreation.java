package me.rockyhawk.commandpanels.classresources;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
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
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionType;

import java.util.*;
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
            if (matraw.split("\\s")[0].equalsIgnoreCase("cps=") || matraw.split("\\s")[0].toLowerCase().equals("cpo=")) {
                skullname = p.getUniqueId().toString();
                mat = Material.PLAYER_HEAD.toString();
            }

            if (matraw.split("\\s")[0].equalsIgnoreCase("hdb=")) {
                skullname = "hdb";
                mat = Material.PLAYER_HEAD.toString();
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
                s = new ItemStack(Objects.requireNonNull(Material.matchMaterial(mat)), 1);
            }

            if (!skullname.equals("no skull") && !skullname.equals("hdb") && !matraw.split("\\s")[0].equalsIgnoreCase("cpo=")) {
                try {
                    SkullMeta meta;
                    if (matraw.split("\\s")[1].equalsIgnoreCase("self")) {
                        //if cps= self
                        meta = (SkullMeta) s.getItemMeta();
                        try {
                            assert meta != null;
                            meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(skullname)));
                        } catch (Exception var23) {
                            p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " material: cps= self"));
                            plugin.debug(var23,p);
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

            if(addNBT && itemSection.contains("nbt")){
                for(String key : Objects.requireNonNull(itemSection.getConfigurationSection("nbt")).getKeys(true)){
                    if(itemSection.isConfigurationSection("nbt." + key)){
                        continue;
                    }
                    s = plugin.nbt.setData(s,key,plugin.tex.attachPlaceholders(panel, position, p, Objects.requireNonNull(itemSection.getString("nbt." + key))));
                }
            }
            if (itemSection.contains("enchanted")) {
                try {
                    ItemMeta EnchantMeta;
                    if(itemSection.isList("enchanted")){
                        //if list contains true, hide enchanted and add KNOCKBACK
                        EnchantMeta = s.getItemMeta();
                        assert EnchantMeta != null;
                        for(String enchantment : itemSection.getStringList("enchanted")){
                            if(enchantment.equalsIgnoreCase("true")) {
                                EnchantMeta.addEnchant(Enchantment.KNOCKBACK, 1, false);
                                EnchantMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                continue;
                            }
                            EnchantMeta.addEnchant(Objects.requireNonNull(EnchantmentWrapper.getByKey(NamespacedKey.minecraft(enchantment.split("\\s")[0].toLowerCase()))), Integer.parseInt(enchantment.split("\\s")[1]), true);
                        }
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
                    PotionType newData = PotionType.valueOf(effectType[0].toUpperCase());
                    //set meta
                    potionMeta.setBasePotionType(newData);
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
                if(itemSection.getString("damage").equalsIgnoreCase("-1")){
                    //if the player wants the item to be unbreakable. Only works in non legacy versions
                    ItemMeta unbreak = s.getItemMeta();
                    unbreak.setUnbreakable(true);
                    s.setItemMeta(unbreak);
                }

                try {
                    Damageable itemDamage = (Damageable) s.getItemMeta();
                    itemDamage.setDamage(Integer.parseInt(Objects.requireNonNull(plugin.tex.placeholders(panel,position,p, itemSection.getString("damage")))));
                    s.setItemMeta(itemDamage);
                } catch (Exception e) {
                    plugin.debug(e, p);
                    p.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.error") + " damage: " + itemSection.getString("damage")));
                }
            }
            if (itemSection.contains("nbt")) {
                for(String key : Objects.requireNonNull(itemSection.getConfigurationSection("nbt")).getKeys(true)){
                    if(itemSection.isConfigurationSection("nbt." + key)){
                        continue;
                    }
                    s = plugin.nbt.setData(s, key, plugin.tex.attachPlaceholders(panel, position, p, Objects.requireNonNull(itemSection.getString("nbt." + key))));
                }
            }
            // 1.20 Trim Feature for Player Armor
            if(itemSection.contains("trim")){
                // trim: <Material> <Pattern>
                String trim = itemSection.getString("trim");
                String[] trimList = trim.split("\\s");
                if(trimList.length == 2){
                    String trimMaterialString = trimList[0].toLowerCase();
                    String trimPatternString = trimList[1].toLowerCase();

                    // Check if Material and Pattern are valid and the itemstack is an armor piece
                    if(isTrimMaterial(trimMaterialString) && isTrimPattern(trimPatternString) && isArmor(s)){

                        // Getting the correct Pattern and Material - Seems to be experimental this way
                        // Material and Pattern don't have a valueOf-function to get them the easier way.
                        TrimMaterial trimMaterial = Registry.TRIM_MATERIAL.get(Objects.requireNonNull(NamespacedKey.fromString("minecraft:" + trimMaterialString)));
                        TrimPattern trimPattern = Registry.TRIM_PATTERN.get(Objects.requireNonNull(NamespacedKey.fromString("minecraft:" + trimPatternString)));

                        ArmorMeta armorMeta = (ArmorMeta) s.getItemMeta();
                        armorMeta.setTrim(new ArmorTrim(trimMaterial, trimPattern));
                        s.setItemMeta(armorMeta);
                    }
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
                if(file.contains("panels." + panelName + ".item." + i + ".material")){
                    if(Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("%") || Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("=")){
                        if(!(Material.PLAYER_HEAD == cont.getType())){
                            file.set("panels." + panelName + ".item." + i + ".material", cont.getType().toString());
                        }
                    }else{
                        file.set("panels." + panelName + ".item." + i + ".material", cont.getType().toString());
                    }
                }else{
                    file.set("panels." + panelName + ".item." + i + ".material", cont.getType().toString());
                }
                if(Material.PLAYER_HEAD == cont.getType()){
                    if(!Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("%") && !Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("=")) {
                        if (plugin.customHeads.getHeadBase64(cont) != null) {
                            //inject base64 here
                            file.set("panels." + panelName + ".item." + i + ".material", "cps= " + plugin.customHeads.getHeadBase64(cont));
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
                try {
                    PotionMeta potionMeta = (PotionMeta) cont.getItemMeta();
                    assert potionMeta != null;
                    String potionType = potionMeta.getBasePotionType().toString(); // Gets the potion type as a string rather than bukkit type
                    file.set("panels." + panelName + ".item." + i + ".potion", potionType);
                }catch(Exception ignore){
                    //not a banner
                    file.set("panels." + panelName + ".item." + i + ".potion", null);
                }
                file.set("panels." + panelName + ".item." + i + ".stack", cont.getAmount());
                if(!cont.getEnchantments().isEmpty()){
                    Map<Enchantment, Integer> enchantments = cont.getEnchantments();
                    List<String> newEnchantments = new ArrayList<>();
                    for(Enchantment enchantment : enchantments.keySet()){
                        newEnchantments.add(enchantment.getKey().getKey() + " " + enchantments.get(enchantment));
                    }
                    file.set("panels." + panelName + ".item." + i + ".enchanted", newEnchantments);
                }
                file.set("panels." + panelName + ".item." + i + ".name", Objects.requireNonNull(cont.getItemMeta()).getDisplayName());
                file.set("panels." + panelName + ".item." + i + ".lore", Objects.requireNonNull(cont.getItemMeta()).getLore());
                file.set("panels." + panelName + ".item." + i + ".customdata", Objects.requireNonNull(cont.getItemMeta()).getCustomModelData());
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
    public boolean isIdentical(ItemStack one, ItemStack two, Boolean nbtCheck){
        //check material
        if (one.getType() != two.getType()) {
            return false;
        }
        if(one.hasItemMeta() != two.hasItemMeta()){
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
        //check for custom model data
        try {
            if (one.getItemMeta().getCustomModelData() != (two.getItemMeta().getCustomModelData())) {
                if(one.getItemMeta().hasCustomModelData()) {
                    return false;
                }
            }
        }catch(Exception ignore){}
        //check for nbt
        if(nbtCheck) {
            try {
                if(!plugin.nbt.hasSameNBT(one, two)){
                    return false;
                }
            } catch (Exception ignore) {}
        }
        //check for damage
        try {
            Damageable tempOne = (Damageable) one.getItemMeta();
            Damageable tempTwo = (Damageable) two.getItemMeta();
            if(tempOne.getDamage() != tempTwo.getDamage()){
                return false;
            }
        } catch (Exception ignore) {}
        //check for potions
        try {
            PotionMeta meta1 = (PotionMeta) one.getItemMeta();
            PotionMeta meta2 = (PotionMeta) two.getItemMeta();
            //different potion type
            if (meta1.getBasePotionType().toString().compareTo(meta2.getBasePotionType().toString()) != 0){
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

    private boolean isTrimMaterial(String material){
        List<String> availableMaterial = Arrays.asList("AMETHYST",
                "COPPER", "DIAMOND", "EMERALD", "GOLD", "IRON","LAPIS", "NETHERITE", "QUARTZ", "REDSTONE");
        return availableMaterial.contains(material.toUpperCase());
    }

    private boolean isTrimPattern(String pattern){
        List<String> availablePattern = Arrays.asList("COAST",
                "DUNE", "EYE", "HOST", "RAISER", "RIB","SENTRY", "SHAPER", "SILENCE", "SNOUT", "SPIRE", "TIDE","VEX", "WARD", "WAYFINDER", "WILD");
        return availablePattern.contains(pattern.toUpperCase());
    }

    private boolean isArmor(ItemStack stack){
        return EnchantmentTarget.ARMOR.includes(stack);
    }
}
