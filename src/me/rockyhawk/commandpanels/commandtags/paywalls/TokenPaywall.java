package me.rockyhawk.commandpanels.commandtags.paywalls;

import me.realized.tokenmanager.api.TokenManager;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.PaywallEvent;
import me.rockyhawk.commandpanels.commandtags.PaywallOutput;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class TokenPaywall implements Listener {
    CommandPanels plugin;
    public TokenPaywall(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(PaywallEvent e){
        if(e.name.equalsIgnoreCase("tokenpaywall=")){
            //if player uses tokenpaywall= [price]
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    final TokenManager api = (TokenManager) Bukkit.getPluginManager().getPlugin("TokenManager");
                    assert api != null;
                    int balance = Integer.parseInt(Long.toString(api.getTokens(e.p).orElse(0)));
                    if (balance >= Double.parseDouble(e.args[0])) {
                        if (e.doDelete) {
                            api.removeTokens(e.p, Long.parseLong(e.args[0]));
                        }
                        //if the message is empty don't send
                        if (plugin.config.getBoolean("purchase.tokens.enable") && e.doDelete) {
                            plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.tokens.success")).replaceAll("%cp-args%", e.args[0]));
                        }

                        e.PAYWALL_OUTPUT = PaywallOutput.Passed;
                    } else {
                        if (plugin.config.getBoolean("purchase.tokens.enable")) {
                            plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.tokens.failure")));
                        }
                        e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
                    }
                } else {
                    plugin.tex.sendString(e.p, plugin.tag + ChatColor.RED + "Needs TokenManager to work!");
                    e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
                }
            } catch (Exception buyc) {
                plugin.debug(buyc, e.p);
                plugin.tex.sendString(e.p, plugin.tag + plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
                e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
            }
        }
    }
}
