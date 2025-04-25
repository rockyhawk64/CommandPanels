package me.rockyhawk.commandpanels.interaction.commands.paywalls;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.PaywallOutput;
import me.rockyhawk.commandpanels.interaction.commands.PaywallResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Paywall implements PaywallResolver {

    @Override
    public PaywallOutput handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command, boolean performOperation) {
        // Only handle commands that start with "paywall="
        if (!command.toLowerCase().startsWith("paywall=")) {
            return PaywallOutput.NotApplicable;
        }

        try {
            if (ctx.econ != null) {
                String[] args = ctx.text.attachPlaceholders(panel, pos, player, command.substring("paywall=".length()).trim()).split(" ");
                if (args.length < 1) {
                    ctx.text.sendString(player, ctx.tag + "Invalid paywall usage. Missing amount.");
                    return PaywallOutput.Blocked;
                }

                double paywallAmount = Double.parseDouble(args[0]);

                if (ctx.econ.getBalance(player) >= paywallAmount) {
                    if (performOperation) {
                        ctx.econ.withdrawPlayer(player, paywallAmount);
                    }

                    if (ctx.configHandler.isTrue("purchase.currency.enable") && performOperation) {
                        String successMsg = ctx.configHandler.config.getString("purchase.currency.success");
                        if (successMsg != null) {
                            ctx.text.sendString(panel, PanelPosition.Top, player,
                                    successMsg.replaceAll("%cp-args%", args[0]));
                        }
                    }

                    return PaywallOutput.Passed;
                } else {
                    if (ctx.configHandler.isTrue("purchase.currency.enable")) {
                        String failureMsg = ctx.configHandler.config.getString("purchase.currency.failure");
                        if (failureMsg != null) {
                            ctx.text.sendString(panel, PanelPosition.Top, player, failureMsg);
                        }
                    }
                    return PaywallOutput.Blocked;
                }
            } else {
                ctx.text.sendString(player, ctx.tag + ChatColor.RED + "Paying Requires Vault and an Economy to work!");
                return PaywallOutput.Blocked;
            }

        } catch (Exception ex) {
            ctx.debug.send(ex, player, ctx);
            ctx.text.sendString(player,
                    ctx.tag + ctx.configHandler.config.getString("config.format.error") + " command: paywall");
            return PaywallOutput.Blocked;
        }
    }
}