package me.rockyhawk.commandpanels.commandtags.paywalls;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.PaywallEvent;
import me.rockyhawk.commandpanels.commandtags.PaywallOutput;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.OptionalLong;

public class TokenPaywall implements Listener {
    private final CommandPanels plugin;

    public TokenPaywall(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(PaywallEvent e) {
        if (e.name.equalsIgnoreCase("tokenpaywall=")) {
            // if player uses tokenpaywall= [price]
            try {
                if (plugin.getServer().getPluginManager().isPluginEnabled("TokenManager")) {
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
                            if (plugin.config.getBoolean("purchase.tokens.enable") && e.doDelete) {
                                plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.tokens.success")).replaceAll("%cp-args%", e.args[0]));
                            }

                            e.PAYWALL_OUTPUT = PaywallOutput.Passed;
                        } else {
                            if (plugin.config.getBoolean("purchase.tokens.enable")) {
                                plugin.tex.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(plugin.config.getString("purchase.tokens.failure")));
                            }
                            e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
                        }
                    } else {
                        plugin.tex.sendString(e.p, plugin.tag + ChatColor.RED + "Needs TokenManager to work!");
                        e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
                    }
                } else {
                    plugin.tex.sendString(e.p, plugin.tag + ChatColor.RED + "Needs TokenManager to work!");
                    e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
                }
            } catch (Exception ex) {
                plugin.debug(ex, e.p);
                plugin.tex.sendString(e.p, plugin.tag + plugin.config.getString("config.format.error") + " " + "commands: " + e.name);
                e.PAYWALL_OUTPUT = PaywallOutput.Blocked;
            }
        }
    }
}
