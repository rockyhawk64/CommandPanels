package me.rockyhawk.commandpanels.commandtags.paywalls;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.PaywallEvent;
import me.rockyhawk.commandpanels.commandtags.PaywallOutput;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class Hasperm implements Listener {
    CommandPanels plugin;
    public Hasperm(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(PaywallEvent e){
        if(e.name.equalsIgnoreCase("hasperm=")){
            //if player uses hasperm= [perm]
            if (e.p.hasPermission(e.args[0])) {
                if (plugin.config.getBoolean("purchase.permission.enable") && e.doDelete) {
                    plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.permission.success")).replaceAll("%cp-args%", e.args[0]));
                }
                e.PAYWALL_OUTPUT = PaywallOutput.Passed;
            } else {
                if (plugin.config.getBoolean("purchase.currency.enable")) {
                    plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.permission.failure")));
                }
                e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
            }
        }
    }
}
