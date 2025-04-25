package me.rockyhawk.commandpanels.interaction.commands.paywalls;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.PaywallOutput;
import me.rockyhawk.commandpanels.interaction.commands.PaywallResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class HasPerm implements PaywallResolver {

    @Override
    public PaywallOutput handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command, boolean performOperation) {
        String[] args;
        boolean normal = false;
        if (command.toLowerCase().startsWith("hasperm=")) {
            args = command.substring("hasperm=".length()).trim().split(" ");
        }else if(command.toLowerCase().startsWith("hasnoperm=")){
            args = command.substring("hasnoperm=".length()).trim().split(" ");
            normal = true;
        }else{
            return PaywallOutput.NotApplicable;
        }

        if (args.length < 1) {
            ctx.text.sendString(player, ctx.tag + "Invalid hasperm/hasnoperm usage. Not enough arguments.");
            return PaywallOutput.Blocked;
        }

        String permission = args[0];
        String success = ctx.configHandler.config.getString("purchase.permission.success").replaceAll("%cp-args%", permission);
        String failure = ctx.configHandler.config.getString("purchase.permission.failure");
        if (player.hasPermission(permission)) {
            if (ctx.configHandler.isTrue("purchase.permission.enable") && performOperation) {
                ctx.text.sendString(panel, PanelPosition.Top, player, normal ? success : failure);
            }
            return normal ? PaywallOutput.Passed : PaywallOutput.Blocked;
        } else {
            if (ctx.configHandler.isTrue("purchase.permission.enable")) {
                ctx.text.sendString(panel, PanelPosition.Top, player, normal ? failure : success);
            }
            return normal ? PaywallOutput.Blocked : PaywallOutput.Passed;
        }
    }
}