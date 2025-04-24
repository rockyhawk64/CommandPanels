package me.rockyhawk.commandpanels.interaction.commands.paywalls;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.PaywallEvent;
import me.rockyhawk.commandpanels.interaction.commands.PaywallOutput;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class Paywall implements Listener {
    Context plugin;
    public Paywall(Context pl) {
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
                        if (plugin.configHandler.isTrue("purchase.currency.enable") && e.doDelete) {
                            plugin.text.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.configHandler.config.getString("purchase.currency.success")).replaceAll("%cp-args%", e.args[0]));
                        }
                        e.PAYWALL_OUTPUT = PaywallOutput.Passed;
                    } else {
                        if (plugin.configHandler.isTrue("purchase.currency.enable")) {
                            plugin.text.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.configHandler.config.getString("purchase.currency.failure")));
                        }
                        e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
                    }
                } else {
                    plugin.text.sendString(e.p, plugin.tag + ChatColor.RED + "Paying Requires Vault and an Economy to work!");
                    e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
                }
            } catch (Exception buyc) {
                plugin.debug.send(buyc, e.p, plugin);
                plugin.text.sendString(e.p, plugin.tag + plugin.configHandler.config.getString("config.format.error") + " " + "commands: " + e.name);
                e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
            }
        }
    }
}
