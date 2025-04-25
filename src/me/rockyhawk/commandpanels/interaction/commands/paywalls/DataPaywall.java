package me.rockyhawk.commandpanels.interaction.commands.paywalls;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.PaywallOutput;
import me.rockyhawk.commandpanels.interaction.commands.PaywallResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class DataPaywall implements PaywallResolver {

    @Override
    public PaywallOutput handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command, boolean performOperation) {
        // Only handle commands that start with "data-paywall="
        if (!command.toLowerCase().startsWith("data-paywall=")) {
            return PaywallOutput.NotApplicable;
        }

        // Strip prefix and split args
        String[] args = command.substring("data-paywall=".length()).trim().split(" ");
        if (args.length < 2) {
            ctx.text.sendString(player, ctx.tag + "Invalid data-paywall usage. Not enough arguments.");
            return PaywallOutput.Blocked;
        }

        try {
            String dataKey = args[0];
            String amountStr = args[1];
            double requiredAmount = Double.parseDouble(amountStr);
            double currentAmount = Double.parseDouble(ctx.panelData.getUserData(player.getUniqueId(), dataKey));

            if (currentAmount >= requiredAmount) {
                if (performOperation) {
                    ctx.panelData.doDataMath(player.getUniqueId(), dataKey,
                            "-" + ctx.text.placeholdersNoColour(panel, PanelPosition.Top, player, amountStr));
                }

                if (ctx.configHandler.isTrue("purchase.data.enable")) {
                    String successMsg = ctx.configHandler.config.getString("purchase.data.success");
                    if (successMsg != null) {
                        ctx.text.sendString(panel, PanelPosition.Top, player,
                                successMsg.replace("%cp-args%", dataKey));
                    }
                }

                return PaywallOutput.Passed;
            } else {
                if (ctx.configHandler.isTrue("purchase.data.enable")) {
                    String failureMsg = ctx.configHandler.config.getString("purchase.data.failure");
                    if (failureMsg != null) {
                        ctx.text.sendString(panel, PanelPosition.Top, player, failureMsg);
                    }
                }
                return PaywallOutput.Blocked;
            }

        } catch (Exception ex) {
            ctx.debug.send(ex, player, ctx);
            ctx.text.sendString(player, ctx.tag + ctx.configHandler.config.getString("config.format.error")
                    + " command: data-paywall");
            return PaywallOutput.Blocked;
        }
    }
}