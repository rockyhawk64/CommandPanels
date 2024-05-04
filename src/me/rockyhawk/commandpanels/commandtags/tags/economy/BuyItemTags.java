package me.rockyhawk.commandpanels.commandtags.tags.economy;

import me.realized.tokenmanager.api.TokenManager;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.CommandTagEvent;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class BuyItemTags implements Listener {
    CommandPanels plugin;
    public BuyItemTags(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("buy=")){
            e.commandTagUsed();
            //if player uses buy= it will be eg. buy= <price> <item> <amount of item> <ID>
            try {
                if (plugin.econ != null) {
                    if (plugin.econ.getBalance(e.p) >= Double.parseDouble(e.args[0])) {
                        plugin.econ.withdrawPlayer(e.p, Double.parseDouble(e.args[0]));
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.currency.success")).replaceAll("%cp-args%", e.args[0]));
                        giveItem(e.p, e.args);
                    } else {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.currency.failure")));

                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Buying Requires Vault and an Economy to work!");
                }
            } catch (Exception buy) {
                plugin.debug(buy,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("tokenbuy=")) {
            e.commandTagUsed();
            //if player uses tokenbuy= it will be eg. tokenbuy= <price> <item> <amount of item> <ID>
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    final TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    assert api != null;
                    int balance = Integer.parseInt(Long.toString(api.getTokens(e.p).orElse(0)));
                    if (balance >= Double.parseDouble(e.args[0])) {
                        api.removeTokens(e.p, Long.parseLong(e.args[0]));
                        plugin.tex.sendMessage(e.p, Objects.requireNonNull(plugin.config.getString("purchase.tokens.success")).replaceAll("%cp-args%", e.args[0]));
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.tokens.success")).replaceAll("%cp-args%", e.args[0]));

                        giveItem(e.p,e.args);
                    } else {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.tokens.failure")));

                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Buying Requires TokenManager to work!");
                }
            } catch (Exception buy) {
                plugin.debug(buy, e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
            }
        }
    }

    private void giveItem(Player p, String[] args){
        plugin.inventorySaver.addItem(p,new ItemStack(Objects.requireNonNull(Material.matchMaterial(args[1])), Integer.parseInt(args[2])));
    }
}
