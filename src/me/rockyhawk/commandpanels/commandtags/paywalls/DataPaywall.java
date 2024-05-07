package me.rockyhawk.commandpanels.commandtags.paywalls;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.PaywallEvent;
import me.rockyhawk.commandpanels.commandtags.PaywallOutput;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class DataPaywall implements Listener {
    CommandPanels plugin;
    public DataPaywall(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(PaywallEvent e){
        if(e.name.equalsIgnoreCase("data-paywall=")){
            //if player uses data-paywall= <data> <amount>
            try {
                if (Double.parseDouble(plugin.panelData.getUserData(e.p.getUniqueId(), e.args[0])) >= Double.parseDouble(e.args[1])) {
                    if (e.doDelete)
                        plugin.panelData.doDataMath(e.p.getUniqueId(), e.args[0], "-" + plugin.tex.placeholdersNoColour(e.panel, PanelPosition.Top, e.p, e.args[1]));
                    //if the message is empty don't send
                    if (plugin.config.getBoolean("purchase.data.enable")) {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.data.success")).replaceAll("%cp-args%", e.args[0]));
                    }
                    e.PAYWALL_OUTPUT = PaywallOutput.Passed;
                } else {
                    if (plugin.config.getBoolean("purchase.data.enable")) {
                        plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.data.failure")));
                    }
                    e.PAYWALL_OUTPUT =  PaywallOutput.Blocked;
                }
            } catch (Exception buyc) {
                plugin.debug(buyc, e.p);
                plugin.tex.sendString(e.p, plugin.tag + plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
                e.PAYWALL_OUTPUT =  PaywallOutput.Blocked;
            }
        }
    }
}
