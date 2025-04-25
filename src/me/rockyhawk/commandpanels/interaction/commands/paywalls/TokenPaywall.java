package me.rockyhawk.commandpanels.interaction.commands.paywalls;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.PaywallOutput;
import me.rockyhawk.commandpanels.interaction.commands.PaywallResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.OptionalLong;

public class TokenPaywall implements PaywallResolver {

    @Override
    public PaywallOutput handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command, boolean performOperation) {
        if (!command.toLowerCase().startsWith("tokenpaywall=")) {
            return PaywallOutput.NotApplicable;
        }

        String[] args = command.substring("tokenpaywall=".length()).trim().split(" ");
        if (args.length < 1) {
            ctx.text.sendString(player, ctx.tag + "Invalid tokenpaywall usage. Not enough arguments.");
            return PaywallOutput.Blocked;
        }

        try {
            if (!Bukkit.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                ctx.text.sendString(player, ctx.tag + ChatColor.RED + "Needs TokenManager to work!");
                return PaywallOutput.Blocked;
            }

            Object api = Bukkit.getPluginManager().getPlugin("TokenManager");
            if (api == null) {
                ctx.text.sendString(player, ctx.tag + ChatColor.RED + "TokenManager plugin not found.");
                return PaywallOutput.Blocked;
            }

            Method getTokensMethod = api.getClass().getMethod("getTokens", Player.class);
            Method removeTokensMethod = api.getClass().getMethod("removeTokens", Player.class, long.class);

            Object result = getTokensMethod.invoke(api, player);
            long balance = (result instanceof OptionalLong)
                    ? ((OptionalLong) result).orElse(0L)
                    : 0L;

            long requiredAmount = Long.parseLong(args[0]);

            if (balance >= requiredAmount) {
                if (performOperation) {
                    removeTokensMethod.invoke(api, player, requiredAmount);
                }

                if (ctx.configHandler.isTrue("purchase.tokens.enable") && performOperation) {
                    String successMsg = ctx.configHandler.config.getString("purchase.tokens.success");
                    if (successMsg != null) {
                        ctx.text.sendString(panel, PanelPosition.Top, player,
                                successMsg.replace("%cp-args%", args[0]));
                    }
                }

                return PaywallOutput.Passed;
            } else {
                if (ctx.configHandler.isTrue("purchase.tokens.enable")) {
                    String failureMsg = ctx.configHandler.config.getString("purchase.tokens.failure");
                    if (failureMsg != null) {
                        ctx.text.sendString(panel, PanelPosition.Top, player, failureMsg);
                    }
                }

                return PaywallOutput.Blocked;
            }

        } catch (Exception ex) {
            ctx.debug.send(ex, player, ctx);
            ctx.text.sendString(player, ctx.tag +
                    ctx.configHandler.config.getString("config.format.error") + " command: tokenpaywall");
            return PaywallOutput.Blocked;
        }
    }
}