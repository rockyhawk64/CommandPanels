package me.rockyhawk.commandpanels.interaction.commands.paywalls;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.PaywallEvent;
import me.rockyhawk.commandpanels.interaction.commands.PaywallOutput;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class Hasperm implements Listener {
    Context ctx;
    public Hasperm(Context pl) {
        this.ctx = pl;
    }

    @EventHandler
    public void commandTag(PaywallEvent e){
        if(e.name.equalsIgnoreCase("hasperm=")){
            //if player uses hasperm= [perm]
            if (e.p.hasPermission(e.args[0])) {
                if (ctx.configHandler.isTrue("purchase.permission.enable") && e.doDelete) {
                    ctx.text.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.permission.success")).replaceAll("%cp-args%", e.args[0]));
                }
                e.PAYWALL_OUTPUT = PaywallOutput.Passed;
            } else {
                if (ctx.configHandler.isTrue("purchase.currency.enable")) {
                    ctx.text.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.permission.failure")));
                }
                e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
            }
        }

        if(e.name.equalsIgnoreCase("hasnoperm=")){
            //if player uses hasnoperm= [perm]
            if (!e.p.hasPermission(e.args[0])) {
                if (ctx.configHandler.isTrue("purchase.permission.enable") && e.doDelete) {
                    ctx.text.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.permission.success")).replaceAll("%cp-args%", e.args[0]));
                }
                e.PAYWALL_OUTPUT = PaywallOutput.Passed;
            } else {
                if (ctx.configHandler.isTrue("purchase.currency.enable")) {
                    ctx.text.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.permission.failure")));
                }
                e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
            }
        }
    }
}
