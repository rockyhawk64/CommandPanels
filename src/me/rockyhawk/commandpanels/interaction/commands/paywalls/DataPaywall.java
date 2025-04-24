package me.rockyhawk.commandpanels.interaction.commands.paywalls;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.PaywallEvent;
import me.rockyhawk.commandpanels.interaction.commands.PaywallOutput;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class DataPaywall implements Listener {
    Context ctx;
    public DataPaywall(Context pl) {
        this.ctx = pl;
    }

    @EventHandler
    public void commandTag(PaywallEvent e){
        if(e.name.equalsIgnoreCase("data-paywall=")){
            //if player uses data-paywall= <data> <amount>
            try {
                if (Double.parseDouble(ctx.panelData.getUserData(e.p.getUniqueId(), e.args[0])) >= Double.parseDouble(e.args[1])) {
                    if (e.doDelete)
                        ctx.panelData.doDataMath(e.p.getUniqueId(), e.args[0], "-" + ctx.text.placeholdersNoColour(e.panel, PanelPosition.Top, e.p, e.args[1]));
                    //if the message is empty don't send
                    if (ctx.configHandler.isTrue("purchase.data.enable")) {
                        ctx.text.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.data.success")).replaceAll("%cp-args%", e.args[0]));
                    }
                    e.PAYWALL_OUTPUT = PaywallOutput.Passed;
                } else {
                    if (ctx.configHandler.isTrue("purchase.data.enable")) {
                        ctx.text.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.data.failure")));
                    }
                    e.PAYWALL_OUTPUT =  PaywallOutput.Blocked;
                }
            } catch (Exception buyc) {
                ctx.debug.send(buyc, e.p, ctx);
                ctx.text.sendString(e.p, ctx.tag + ctx.configHandler.config.getString("config.format.error") + " " + "commands: " + e.name);
                e.PAYWALL_OUTPUT =  PaywallOutput.Blocked;
            }
        }
    }
}
