package me.rockyhawk.commandpanels.commandtags.paywalls;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.PaywallEvent;
import me.rockyhawk.commandpanels.commandtags.PaywallOutput;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class Paywall implements Listener {
    CommandPanels plugin;
    public Paywall(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(PaywallEvent e){
        if(e.name.equalsIgnoreCase("paywall=")){
            //if player uses paywall= [price]
            try {
                if (plugin.econ != null) {
                    double paywallAmount = Double.parseDouble(e.args[0]);
                    if (plugin.econ.getBalance(e.p) >= paywallAmount) {
                        if (e.doDelete) plugin.econ.withdrawPlayer(e.p, paywallAmount);
                        if (plugin.config.getBoolean("purchase.currency.enable") && e.doDelete) {
                            plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.currency.success")).replaceAll("%cp-args%", e.args[0]));
                        }
                        e.PAYWALL_OUTPUT = PaywallOutput.Passed;
                    } else {
                        if (plugin.config.getBoolean("purchase.currency.enable")) {
                            plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.currency.failure")));
                        }
                        e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
                    }
                } else {
                    plugin.tex.sendString(e.p, plugin.tag + ChatColor.RED + "Paying Requires Vault and an Economy to work!");
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
