package me.rockyhawk.commandpanels.commandtags.tags.economy;

import me.realized.tokenmanager.api.TokenManager;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.CommandTagEvent;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.Objects;

public class BuyCommandTags implements Listener {
    CommandPanels plugin;
    public BuyCommandTags(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("buycommand=")){
            e.commandTagUsed();
            //if player uses buycommand [price] [command]
            try {
                if (plugin.econ != null) {
                    if (plugin.econ.getBalance(e.p) >= Double.parseDouble(e.args[0])) {
                        plugin.econ.withdrawPlayer(e.p, Double.parseDouble(e.args[0]));
                        //execute command under here
                        String price = e.args[0];
                        String command = String.join(" ",Arrays.copyOfRange(e.raw, 1, e.raw.length));
                        plugin.commandTags.runCommand(e.panel,e.pos,e.p,command);
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.currency.success")).replaceAll("%cp-args%", price));
                    } else {
                        String price = e.args[0];
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.currency.failure")).replaceAll("%cp-args%", price));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Buying Requires Vault and an Economy to work!");
                }
            } catch (Exception buyc) {
                plugin.debug(buyc,e.p);
                plugin.tex.sendMessage(e.p,plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("tokenbuycommand=")){
            e.commandTagUsed();
            //if player uses tokenbuycommand [price] [command]
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    TokenManager api = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                    assert api != null;
                    int balance = Integer.parseInt(Long.toString(api.getTokens(e.p).orElse(0)));
                    if (balance >= Double.parseDouble(e.args[0])) {
                        api.removeTokens(e.p, Long.parseLong(e.args[0]));
                        //execute command under here
                        String price = e.args[0];
                        String command = String.join(" ",Arrays.copyOfRange(e.raw, 1, e.raw.length));
                        plugin.commandTags.runCommand(e.panel,e.pos,e.p,command);
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.tokens.success")).replaceAll("%cp-args%", price));
                    } else {
                        String price = e.args[0];
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.tokens.failure")).replaceAll("%cp-args%", price));
                    }
                } else {
                    plugin.tex.sendMessage(e.p, ChatColor.RED + "Buying Requires Vault and an Economy to work!");
                }
            } catch (Exception buyc) {
                plugin.debug(buyc,e.p);
                plugin.tex.sendMessage(e.p, plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
            }
        }
    }
}
