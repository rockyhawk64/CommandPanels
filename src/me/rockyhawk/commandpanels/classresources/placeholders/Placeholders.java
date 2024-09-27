package me.rockyhawk.commandpanels.classresources.placeholders;

import com.earth2me.essentials.Essentials;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.PotionMeta;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

public class Placeholders {
    CommandPanels plugin;
    public Placeholders(CommandPanels pl) {
        this.plugin = pl;
    }

    public String setPlaceholders(Panel panel,PanelPosition position, Player p, String str, boolean primary){
        String[] HOLDERS = getPlaceholderEnds(panel,primary);
        while (str.contains(HOLDERS[0] + "cp-")) {
            try {
                int start = str.indexOf(HOLDERS[0] + "cp-");
                int end = str.indexOf(HOLDERS[1], str.indexOf(HOLDERS[0] + "cp-") + 1);
                String identifier = str.substring(start, end).replace(HOLDERS[0] + "cp-", "").replace(HOLDERS[1], "");
                String value;
                try {
                    value = cpPlaceholders(panel,position,p,identifier);
                } catch (NullPointerException er) {
                    value = "";
                }
                str = str.replace(str.substring(start, end) + HOLDERS[1], value);
            }catch(Exception ex){
                plugin.debug(ex,p);
                break;
            }
        }
        return str;
    }

    //returns primary then secondary {[start,end],[start,end]}
    public String[] getPlaceholderEnds(Panel panel, boolean primary){
        List<String[]> values = new ArrayList<>();
        values.add(new String[]{plugin.config.getString("placeholders.primary.start"),plugin.config.getString("placeholders.primary.end")});
        values.add(new String[]{plugin.config.getString("placeholders.secondary.start"),plugin.config.getString("placeholders.secondary.end")});
        if(panel != null) {
            if (panel.getConfig().isSet("placeholders")) {
                if (panel.getConfig().isSet("placeholders.primary")) {
                    values.set(0, new String[]{panel.getConfig().getString("placeholders.primary.start"), panel.getConfig().getString("placeholders.primary.end")});
                }
                if (panel.getConfig().isSet("placeholders.secondary")) {
                    values.set(1, new String[]{panel.getConfig().getString("placeholders.secondary.start"), panel.getConfig().getString("placeholders.secondary.end")});
                }
            }
        }
        if(primary){
            return values.get(0);
        }else{
            return values.get(1);
        }
    }

    //this requires the placeholder to already be identified
    @SuppressWarnings("deprecation")
    public String cpPlaceholders(Panel panel, PanelPosition position, Player p, String identifier){

        //replace nodes with PlaceHolders
        switch(identifier){
            case("player-displayname"): {
                return p.getDisplayName();
            }
            case("player-name"): {
                return p.getName();
            }
            case("player-world"): {
                return p.getWorld().getName();
            }
            case("player-x"): {
                return String.valueOf(Math.round(p.getLocation().getX()));
            }
            case("player-y"): {
                return String.valueOf(Math.round(p.getLocation().getY()));
            }
            case("player-z"): {
                return String.valueOf(Math.round(p.getLocation().getZ()));
            }
            case("online-players"): {
                return Integer.toString(Bukkit.getServer().getOnlinePlayers().size());
            }
            case("online-players-visible"): {
                //will not include players that are vanished
                int count = 0;
                for(Player temp : Bukkit.getOnlinePlayers()) {
                    if(!isPlayerVanished(temp)) {
                        count++;
                    }
                }
                return Integer.toString(count);
            }
            case("panel-position"): {
                return position.toString();
            }
            case("tag"): {
                return plugin.tex.colour(plugin.tag);
            }
        }

        //set custom placeholders to their values
        if(panel != null) {
            for (String placeholder : panel.placeholders.keys.keySet()) {
                if(identifier.equals(placeholder)) {
                    try {
                        return panel.placeholders.keys.get(placeholder);
                    } catch (Exception ex) {
                        plugin.debug(ex, p);
                        break;
                    }
                }
            }
        }

        //placeholder to check for server availability %cp-server-IP:PORT%
        if(identifier.startsWith("server-")) {
            String ip_port = identifier.replace("server-", "");
            Socket s = new Socket();
            try {
                s.connect(new InetSocketAddress(ip_port.split(":")[0], (int)Double.parseDouble(ip_port.split(":")[1])), plugin.config.getInt("config.server-ping-timeout"));
                s.close();
                return "true";
            }catch (IOException ex){
                return "false";
            }
        }

        //placeholder to check if an item has NBT %cp-nbt-slot:type:key%
        if (identifier.startsWith("nbt-")) {
            try {
                String slot_key = identifier.replace("nbt-", "");
                Object value;
                value = plugin.nbt.getNBT(
                        p.getOpenInventory().getTopInventory().getItem(
                                (int) Double.parseDouble(slot_key.split(":")[0])
                        ),
                        slot_key.split(":")[2],
                        slot_key.split(":")[1]
                );
                // Convert any object type to a string, handle null explicitly if desired
                return value == null ? "empty" : String.valueOf(value);
            } catch (Exception ex) {
                plugin.debug(ex, p);
                return ""; // Consider returning "error" or some other indicative string
            }
        }

        // Placeholder to check if an item has POTION data %cp-potion-slot%
        if (identifier.startsWith("potion-")) {
            try {
                String slot_key = identifier.replace("potion-", "");
                int slotIndex = (int) Double.parseDouble(slot_key);

                // Get the item in the specified slot
                ItemStack item = p.getOpenInventory().getTopInventory().getItem(slotIndex);

                // Check if the item is not null and has potion meta
                if (item != null && item.hasItemMeta() && item.getItemMeta() instanceof PotionMeta) {
                    //choose between legacy PotionData (pre 1.20.5) or PotionType
                    if(plugin.legacy.MAJOR_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_19) ||
                            (plugin.legacy.MAJOR_VERSION == MinecraftVersions.v1_20 && plugin.legacy.MINOR_VERSION <= 4)){
                        //Returns the value like this <Type>:<Extended>:<Upgraded> Example SLOWNESS:true:false
                        return plugin.legacyPotion.retrievePotionData(item).replaceAll("\\s",":");
                    }else{
                        //post 1.20.5 compare just return PotionType
                        PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
                        return potionMeta.getBasePotionType().toString();
                    }
                } else {
                    return "empty"; // Item is either null or doesn't have potion meta
                }
            } catch (Exception ex) {
                plugin.debug(ex, p);
                return ""; // Handle exceptions as needed
            }
        }

        //DO placeholders for detection of other items in a panel
        //get material value from slot in current open inventory (panel)
        if(identifier.startsWith("material-")) {
            try {
                String matNumber = identifier.replace("material-", "");
                String material;
                try {
                    material = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber)).getType().toString();
                    if (plugin.legacy.MAJOR_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)) {
                        //add the ID to the end if it is legacy (eg, material:id)
                        material = material + ":" + p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber)).getData().getData();
                    }
                } catch (NullPointerException er) {
                    material = "AIR";
                }
                return material;
            } catch (Exception ex) {
                plugin.debug(ex,p);
                return "";
            }
        }
        //get name value from slot in current open inventory (panel)
        if(identifier.startsWith("name-")) {
            try {
                String nameNumber = identifier.replace("name-", "");
                String name;
                try {
                    ItemStack item = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(nameNumber));
                    name = item.getType().toString().replace("_"," ");
                    if(item.hasItemMeta()){
                        if(item.getItemMeta().hasDisplayName()){
                            name = item.getItemMeta().getDisplayName();
                        }
                    }
                } catch (NullPointerException er) {
                    name = "";
                }
                return name;
            } catch (Exception ex) {
                plugin.debug(ex,p);
                return "";
            }
        }
        //get lore value from slot in current open inventory (panel)
        if(identifier.startsWith("lore-")) {
            try {
                String loreNumber = identifier.replace("lore-", "");
                String lore = "";
                try {
                    ItemStack item = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(loreNumber));
                    if(item.hasItemMeta()){
                        if(item.getItemMeta().hasLore()){
                            List<String> ListLore = item.getItemMeta().getLore();
                            for(String list : ListLore){
                                lore = lore + list + "\n";
                            }
                        }
                    }
                } catch (NullPointerException er) {
                    lore = "";
                }
                return lore;
            } catch (Exception ex) {
                plugin.debug(ex,p);
                return "";
            }
        }
        //get stack amount from slot in current open inventory (panel)
        if(identifier.startsWith("stack-")) {
            try {
                String matNumber = identifier.replace("stack-", "");
                int amount;
                try {
                    amount = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber)).getAmount();
                } catch (NullPointerException er) {
                    amount = 0;
                }
                return String.valueOf(amount);
            }catch(Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }
        //get stack amount from slot in current open inventory (panel)
        if(identifier.startsWith("modeldata-")) {
            try {
                String matNumber = identifier.replace("modeldata-", "");
                int modelData;
                try {
                    modelData = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber)).getItemMeta().getCustomModelData();
                } catch (NullPointerException er) {
                    modelData = 0;
                }
                return String.valueOf(modelData);
            }catch(Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }
        //is an item damaged
        if(identifier.startsWith("damaged-")) {
            try {
                String matNumber = identifier.replace("damaged-", "");
                boolean damaged = false;
                ItemStack itm = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber));
                try {
                    if(plugin.legacy.MAJOR_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)){
                        if(itm.getType().getMaxDurability() != 0) {
                            damaged = (itm.getType().getMaxDurability() - itm.getDurability()) < itm.getType().getMaxDurability();
                        }
                    }else {
                        Damageable itemDamage = (Damageable) itm.getItemMeta();
                        damaged = itemDamage.hasDamage();
                    }
                } catch (NullPointerException er) {
                    damaged = false;
                }
                return String.valueOf(damaged);
            }catch(Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }
        //is an item identical, uses custom-items (custom item, slot)
        if(identifier.startsWith("identical-")) {
            try {
                String matLocSlot = identifier.replace("identical-", "");
                String matLoc = matLocSlot.split(",")[0];
                int matSlot = (int)Double.parseDouble(matLocSlot.split(",")[1]);
                boolean isIdentical = false;
                ItemStack itm = p.getOpenInventory().getTopInventory().getItem(matSlot);

                if(itm == null){
                    //continue if material is null
                    return "false";
                }

                try {
                    //if it is a regular custom item
                    ItemStack confItm = plugin.itemCreate.makeItemFromConfig(panel,position,panel.getConfig().getConfigurationSection("custom-item." + matLoc),p,true,true, false);
                    if(plugin.itemCreate.isIdentical(confItm,itm, Objects.requireNonNull(panel.getConfig().getConfigurationSection("custom-item." + matLoc)).contains("nbt"))){
                        isIdentical = true;
                    }

                    //if custom item is an mmo item (1.14+ for the API)
                    String customItemMaterial = panel.getConfig().getString("custom-item." + matLoc + ".material");
                    if (plugin.getServer().getPluginManager().isPluginEnabled("MMOItems") && customItemMaterial.startsWith("mmo=")) {
                        String mmoType = customItemMaterial.split("\\s")[1];
                        String mmoID = customItemMaterial.split("\\s")[2];

                        if (plugin.isMMOItem(itm,mmoType,mmoID) && itm.getAmount() <= confItm.getAmount()) {
                            isIdentical = true;
                        }
                    }
                } catch (NullPointerException er) {
                    isIdentical = false;
                }

                return String.valueOf(isIdentical);
            }catch(Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }

        //does %cp-random-MIN,MAX%
        if(identifier.startsWith("random-")) {
            try {
                String min_max = identifier.replace("random-", "");
                int min = (int)Double.parseDouble(min_max.split(",")[0]);
                int max = (int)Double.parseDouble(min_max.split(",")[1]);
                return String.valueOf(plugin.getRandomNumberInRange(min, max));
            }catch (Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }
        //returns value of stored data
        if(identifier.startsWith("data-")) {
            try {
                String dataPoint = identifier.replace("data-", "");
                //get data from other user
                if(dataPoint.contains(",")){
                    String dataName = dataPoint.split(",")[0];
                    String playerName = dataPoint.split(",")[1];
                    return plugin.panelData.getUserData(plugin.panelDataPlayers.getOffline(playerName),dataName);
                }else{
                    return plugin.panelData.getUserData(p.getUniqueId(),dataPoint);
                }
            }catch (Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }
        //returns if a player is found
        if(identifier.startsWith("uuid-")) {
            try {
                String dataPoint = identifier.replace("uuid-", "");
                //get data from other user
                if(plugin.panelDataPlayers.getOffline(dataPoint) == null){
                    return "unknown";
                }
                return plugin.panelDataPlayers.getOffline(dataPoint).toString();
            }catch (Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }
        //edits data via placeholder execution (will return empty output)
        if(identifier.startsWith("setdata-")) {
            try {
                String point_value = identifier.replace("setdata-", "");
                String command = "set-data= " + point_value.split(",")[0] + " " + point_value.split(",")[1];
                plugin.commandRunner.runCommand(panel,position,p, command);
                return "";
            }catch (Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }
        //math data via placeholder execution (will return empty output)
        if(identifier.startsWith("mathdata-")) {
            try {
                String point_value = identifier.replace("mathdata-", "");
                String command = "math-data= " + point_value.split(",")[0] + " " + point_value.split(",")[1];
                plugin.commandRunner.runCommand(panel,position,p,command);
                return "";
            }catch (Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }

        //checks for players online
        if(identifier.startsWith("player-online-")) {
            try {
                String playerLocation = identifier.replace("player-online-", "");
                if (identifier.endsWith("-visible")){
                    //for players that are visible only
                    //remove -visible from the end of playerLocation
                    playerLocation = playerLocation.replace("-visible", "");
                    List<Player> playerList = new ArrayList<>();
                    for(Player temp : Bukkit.getOnlinePlayers()) {
                        if(!isPlayerVanished(temp)) {
                            playerList.add(temp);
                        }
                    }
                    if(playerList.size() >= Integer.parseInt(playerLocation)){
                        return playerList.get(Integer.parseInt(playerLocation)-1).getName();
                    }
                } else {
                    //for every player whether they are visible or not
                    if(Bukkit.getOnlinePlayers().toArray().length >= Integer.parseInt(playerLocation)){
                        return ((Player)Bukkit.getOnlinePlayers().toArray()[Integer.parseInt(playerLocation)-1]).getName();
                    }
                }
                //player is not found
                return plugin.tex.colour(Objects.requireNonNull(plugin.config.getString("config.format.offline")));
            }catch (Exception ex){
                plugin.debug(ex,p);
                return "";
            }
        }

        try {
            if (plugin.econ != null) {
                if(identifier.equals("player-balance")) {
                    return String.valueOf(Math.round(plugin.econ.getBalance(p)));
                }
            }
        } catch (Exception place) {
            //skip
        }
        //end nodes with PlaceHolders
        return "";
    }

    public boolean isPlayerVanished(Player player) {
        //check if EssentialsX exists
        if(!Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            return false;
        }
        //check if player is vanished using essentials
        Essentials essentials = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");
        return essentials.getUser(player).isVanished();
    }
}
