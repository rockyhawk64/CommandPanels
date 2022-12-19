package me.rockyhawk.commandpanels.commandtags.tags.economy;

import de.NeonnBukkit.CoinsAPI.API.CoinsAPI;
import me.realized.tokenmanager.api.TokenManager;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.CommandTagEvent;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SellItemTags implements Listener {
    CommandPanels plugin;
    public SellItemTags(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("sell=")){
            e.commandTagUsed();
            //if player uses sell= it will be eg. sell= <cashback> <item> <amount of item> [enchanted:KNOCKBACK:1] [potion:JUMP]
            try {
                if (plugin.econ != null) {
                    boolean sold = removeItem(e.p, e.args);
                    if (!sold) {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.failure")).replaceAll("%cp-args%", e.args[1]));
                    } else {
                        plugin.econ.depositPlayer(e.p, Double.parseDouble(e.args[0]));
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%", e.args[1]));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Selling Requires Vault and an Economy to work!");
                }
            } catch (Exception sell) {
                plugin.debug(sell,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("tokensell=")) {
            e.commandTagUsed();
            //if player uses tokensell= it will be eg. tokensell= <cashback> <item> <amount of item> [enchanted:KNOCKBACK:1] [potion:JUMP]
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    boolean sold = removeItem(e.p, e.args);
                    if (!sold) {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.failure")).replaceAll("%cp-args%", e.args[1]));
                    } else {
                        assert api != null;
                        api.addTokens(e.p, Long.parseLong(e.args[0]));
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%", e.args[1]));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Selling Requires TokenManager to work!");
                }
            } catch (Exception sell) {
                plugin.debug(sell,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
            }
        }
        if(e.name.equalsIgnoreCase("coinsell=")) {
            e.commandTagUsed();
            //if player uses coinsell= it will be eg. coinsell= <cashback> <item> <amount of item> [enchanted:KNOCKBACK:1] [potion:JUMP]
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("CoinsAPINB")) {
                    boolean sold = removeItem(e.p, e.args);
                    if (!sold) {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.failure")).replaceAll("%cp-args%", e.args[1]));
                    } else {
                        CoinsAPI.addCoins(e.p.getUniqueId().toString(), (int) Long.parseLong(e.args[0]));
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.item.success")).replaceAll("%cp-args%", e.args[1]));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Selling Requires CoinsAPINB to work!");
                }
            } catch (Exception sell) {
                plugin.debug(sell,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
            }
        }
    }

    //returns false if player does not have item
    private boolean removeItem(Player p, String[] args){
        List<ItemStack> cont = new ArrayList<>(Arrays.asList(plugin.inventorySaver.getNormalInventory(p)));

        for (int f = 0; f < 36; f++) {
            ItemStack itm = cont.get(f);
            if (itm != null && itm.getType().equals(Material.matchMaterial(args[1]))) {
                //determine if the command contains parameters for extensions
                String potion = "false";
                for(String argsTemp : args){
                    if(argsTemp.startsWith("potion:")){
                        potion = argsTemp.replace("potion:","");
                    }
                }
                //legacy ID
                byte id = -1;
                if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)) {
                    for (String argsTemp : args) {
                        if (argsTemp.startsWith("id:")) {
                            id = Byte.parseByte(argsTemp.replace("id:", ""));
                            break;
                        }
                    }
                }
                //check to ensure any extensions are checked
                try {
                    if (!potion.equals("false")) {
                        PotionMeta potionMeta = (PotionMeta) itm.getItemMeta();
                        assert potionMeta != null;
                        if (!potionMeta.getBasePotionData().getType().name().equalsIgnoreCase(potion)) {
                            p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Your item has the wrong potion effect"));
                            return false;
                        }
                    }
                    if (id != -1) {
                        if (itm.getDurability() != id) {
                            continue;
                        }
                    }
                }catch(Exception exc){
                    //skip if it cannot do unless plugin.debug is enabled
                    plugin.debug(exc,p);
                }
                if (itm.getAmount() >= new ItemStack(Objects.requireNonNull(Material.matchMaterial(args[1])), Integer.parseInt(args[2])).getAmount()) {
                    int amt = itm.getAmount() - new ItemStack(Objects.requireNonNull(Material.matchMaterial(args[1])), Integer.parseInt(args[2])).getAmount();
                    itm.setAmount(amt);
                    if(plugin.inventorySaver.hasNormalInventory(p)){
                        p.getInventory().setItem(f, amt > 0 ? itm : null);
                        p.updateInventory();
                    }else{
                        cont.set(f,amt > 0 ? itm : null);
                        plugin.inventorySaver.inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
