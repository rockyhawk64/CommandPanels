package me.rockyhawk.commandpanels.classresources;

import com.bencodez.votingplugin.user.UserManager;
import me.realized.tokenmanager.api.TokenManager;
import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Placeholders {
    CommandPanels plugin;
    public Placeholders(CommandPanels pl) {
        this.plugin = pl;
    }

    @SuppressWarnings("deprecation")
    public String setCpPlaceholders(Player p, String str) {
        //replace nodes with PlaceHolders
        str = str.replaceAll("%cp-player-displayname%", p.getDisplayName());
        str = str.replaceAll("%cp-player-name%", p.getName());
        str = str.replaceAll("%cp-player-world%", p.getWorld().getName());
        str = str.replaceAll("%cp-player-x%", String.valueOf(Math.round(p.getLocation().getX())));
        str = str.replaceAll("%cp-player-y%", String.valueOf(Math.round(p.getLocation().getY())));
        str = str.replaceAll("%cp-player-z%", String.valueOf(Math.round(p.getLocation().getZ())));
        str = str.replaceAll("%cp-online-players%", Integer.toString(Bukkit.getServer().getOnlinePlayers().size()));
        str = str.replaceAll("%cp-tag%", plugin.papi(plugin.tag));
        //placeholder to check for server availability %cp-server-IP:PORT%
        while (str.contains("%cp-server-")) {
            int start = str.indexOf("%cp-server-");
            int end = str.indexOf("%", str.indexOf("%cp-server-")+1);
            String ip_port = str.substring(start, end).replace("%cp-server-", "").replace("%","");
            Socket s = new Socket();
            try {
                s.connect(new InetSocketAddress(ip_port.split(":")[0], Integer.parseInt(ip_port.split(":")[1])), plugin.config.getInt("config.server-ping-timeout"));
                str = str.replace(str.substring(start, end) + "%", "true");
                s.close();
            }catch (IOException ex){
                str = str.replace(str.substring(start, end) + "%", "false");
            }
        }

        //set custom placeholders to their values
        for(String[] placeholder : plugin.customCommand.getCCP(p.getName())){
            while (str.contains(placeholder[0])) {
                int start = str.indexOf(placeholder[0]);
                int end = start+placeholder[0].length()-1;
                str = str.replace(str.substring(start, end) + "%", placeholder[1]);
            }
        }

        //DO placeholders for detection of other items in a panel
        //get material value from slot in current open inventory (panel)
        while (str.contains("%cp-material-")) {
            try {
                int start = str.indexOf("%cp-material-");
                int end = str.indexOf("%", str.indexOf("%cp-material-") + 1);
                String matNumber = str.substring(start, end).replace("%cp-material-", "").replace("%", "");
                String material;
                try {
                    material = p.getOpenInventory().getTopInventory().getItem(Integer.parseInt(matNumber)).getType().toString();
                    if(plugin.legacy.isLegacy()){
                        //add the ID to the end if it is legacy (eg, material:id)
                        material = material + ":" + p.getOpenInventory().getTopInventory().getItem(Integer.parseInt(matNumber)).getType().getId();
                    }
                } catch (NullPointerException er) {
                    material = "AIR";
                }
                str = str.replace(str.substring(start, end) + "%", material);
            }catch(Exception ex){
                plugin.debug(ex);
                break;
            }
        }
        //get stack amount from slot in current open inventory (panel)
        while (str.contains("%cp-stack-")) {
            try {
                int start = str.indexOf("%cp-stack-");
                int end = str.indexOf("%", str.indexOf("%cp-stack-") + 1);
                String matNumber = str.substring(start, end).replace("%cp-stack-", "").replace("%", "");
                int amount;
                try {
                    amount = p.getOpenInventory().getTopInventory().getItem(Integer.parseInt(matNumber)).getAmount();
                } catch (NullPointerException er) {
                    amount = 0;
                }
                str = str.replace(str.substring(start, end) + "%", String.valueOf(amount));
            }catch(Exception ex){
                plugin.debug(ex);
                break;
            }
        }
        //is an item damaged
        while (str.contains("%cp-damaged-")) {
            try {
                int start = str.indexOf("%cp-damaged-");
                int end = str.indexOf("%", str.indexOf("%cp-damaged-") + 1);
                String matNumber = str.substring(start, end).replace("%cp-damaged-", "").replace("%", "");
                boolean damaged = false;
                ItemStack itm = p.getOpenInventory().getTopInventory().getItem(Integer.parseInt(matNumber));
                try {
                    if(plugin.legacy.isLegacy()){
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
                str = str.replace(str.substring(start, end) + "%", String.valueOf(damaged));
            }catch(Exception ex){
                plugin.debug(ex);
                break;
            }
        }
        //is an item identical, uses custom-items (custom item, slot)
        while (str.contains("%cp-identical-")) {
            try {
                int start = str.indexOf("%cp-identical-");
                int end = str.indexOf("%", str.indexOf("%cp-identical-") + 1);
                String matLocSlot = str.substring(start, end).replace("%cp-identical-", "").replace("%", "");
                String matLoc = matLocSlot.split(",")[0];
                int matSlot = Integer.parseInt(matLocSlot.split(",")[1]);
                boolean isIdentical = false;
                ItemStack itm = p.getOpenInventory().getTopInventory().getItem(matSlot);

                try {
                    //if it is a regular custom item
                    ItemStack confItm = plugin.itemCreate.makeItemFromConfig(plugin.openPanels.getOpenPanel(p.getName()).getConfigurationSection("custom-item." + matLoc),p,true,true, true);
                    if(plugin.itemCreate.isIdentical(confItm,itm)){
                        isIdentical = true;
                    }

                    //if custom item is an mmo item (1.14+ for the API)
                    String customItemMaterial = plugin.openPanels.getOpenPanel(p.getName()).getString("custom-item." + matLoc + ".material");
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

                str = str.replace(str.substring(start, end) + "%", String.valueOf(isIdentical));
            }catch(Exception ex){
                plugin.debug(ex);
                break;
            }
        }

        //does %cp-random-MIN,MAX%
        while (str.contains("%cp-random-")) {
            try {
                int start = str.indexOf("%cp-random-");
                int end = str.indexOf("%", str.indexOf("%cp-random-") + 1);
                String min_max = str.substring(start, end).replace("%cp-random-", "").replace("%", "");
                int min = Integer.parseInt(min_max.split(",")[0]);
                int max = Integer.parseInt(min_max.split(",")[1]);
                str = str.replace(str.substring(start, end) + "%", String.valueOf(plugin.getRandomNumberInRange(min, max)));
            }catch (Exception ex){
                plugin.debug(ex);
                break;
            }
        }
        while (str.contains("%cp-player-online-")) {
            try {
                int start = str.indexOf("%cp-player-online-");
                int end = str.indexOf("-find%", str.indexOf("%cp-player-online-") + 1);
                String playerLocation = str.substring(start, end).replace("%cp-player-online-", "");
                Player[] playerFind = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
                if (Integer.parseInt(playerLocation) > playerFind.length) {
                    str = str.replace(str.substring(start, end) + "-find%", plugin.papi(Objects.requireNonNull(plugin.config.getString("config.format.offline"))));
                } else {
                    str = str.replace(str.substring(start, end) + "-find%", playerFind[Integer.parseInt(playerLocation) - 1].getName());
                }
            }catch (Exception ex){
                plugin.debug(ex);
                break;
            }
        }

        try {
            if (plugin.econ != null) {
                str = str.replaceAll("%cp-player-balance%", String.valueOf(Math.round(plugin.econ.getBalance(p))));
            }
        } catch (Exception place) {
            //skip
        }
        if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
            TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
            assert api != null;
            str = str.replaceAll("%cp-tokenmanager-balance%", Long.toString(api.getTokens(p).orElse(0)));
        }
        if (plugin.getServer().getPluginManager().isPluginEnabled("VotingPlugin")) {
            str = str.replaceAll("%cp-votingplugin-points%", String.valueOf(UserManager.getInstance().getVotingPluginUser(p).getPoints()));
        }
        if (str.contains("%cp-player-input%")) {
            for (String[] key : plugin.userInputStrings) {
                if (key[0].equals(p.getName())) {
                    plugin.userInputStrings.add(new String[]{p.getName(), str});
                    return "cpc";
                }
            }
            plugin.userInputStrings.add(new String[]{p.getName(), str});
            List<String> inputMessages = new ArrayList<String>(plugin.config.getStringList("config.input-message"));
            for (String temp : inputMessages) {
                temp = temp.replaceAll("%cp-args%", Objects.requireNonNull(plugin.config.getString("config.input-cancel")));
                p.sendMessage(plugin.papi(p, temp));
            }
            str = "cpc";
        }
        //end nodes with PlaceHolders
        return str;
    }
}
