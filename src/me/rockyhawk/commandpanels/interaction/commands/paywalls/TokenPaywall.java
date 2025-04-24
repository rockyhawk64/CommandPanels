package me.rockyhawk.commandpanels.interaction.commands.paywalls;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.PaywallEvent;
import me.rockyhawk.commandpanels.interaction.commands.PaywallOutput;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.OptionalLong;

public class TokenPaywall implements Listener {
    private final Context ctx;

    public TokenPaywall(Context pl) {
        this.ctx = pl;
    }

    @EventHandler
    public void commandTag(PaywallEvent e) {
        if (e.name.equalsIgnoreCase("tokenpaywall=")) {
            // if player uses tokenpaywall= [price]
            try {
                if (Bukkit.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
                    // Using reflection in this method as TokenManager has issues with Maven due to the Jitpack dependency
                    Object api = Bukkit.getPluginManager().getPlugin("TokenManager");
                    if (api != null) {
                        // Use reflection to access the getTokens and removeTokens methods
                        Method getTokensMethod = api.getClass().getMethod("getTokens", org.bukkit.entity.Player.class);
                        Method removeTokensMethod = api.getClass().getMethod("removeTokens", org.bukkit.entity.Player.class, long.class);

                        // Call getTokens
                        Object result = getTokensMethod.invoke(api, e.p);
                        long balance;

                        if (result instanceof OptionalLong) {
                            balance = ((OptionalLong) result).orElse(0L);
                        } else {
                            balance = 0L;
                        }

                        if (balance >= Double.parseDouble(e.args[0])) {
                            if (e.doDelete) {
                                // Call removeTokens
                                removeTokensMethod.invoke(api, e.p, Long.parseLong(e.args[0]));
                            }
                            // if the message is empty don't send
                            if (ctx.configHandler.isTrue("purchase.tokens.enable") && e.doDelete) {
                                ctx.text.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.tokens.success")).replaceAll("%cp-args%", e.args[0]));
                            }

                            e.PAYWALL_OUTPUT = PaywallOutput.Passed;
                        } else {
                            if (ctx.configHandler.isTrue("purchase.tokens.enable")) {
                                ctx.text.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.tokens.failure")));
                            }
                            e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
                        }
                    } else {
                        ctx.text.sendString(e.p, ctx.tag + ChatColor.RED + "Needs TokenManager to work!");
                        e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
                    }
                } else {
                    ctx.text.sendString(e.p, ctx.tag + ChatColor.RED + "Needs TokenManager to work!");
                    e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
                }
            } catch (Exception ex) {
                ctx.debug.send(ex, e.p, ctx);
                ctx.text.sendString(e.p, ctx.tag + ctx.configHandler.config.getString("config.format.error") + " " + "commands: " + e.name);
                e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
            }
        }
    }
}
