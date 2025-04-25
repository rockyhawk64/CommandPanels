package me.rockyhawk.commandpanels.interaction.commands.paywalls;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.PaywallOutput;
import me.rockyhawk.commandpanels.interaction.commands.PaywallResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class XpPaywall implements PaywallResolver {

    @Override
    public PaywallOutput handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command, boolean performOperation) {
        if (!command.toLowerCase().startsWith("xp-paywall=")) {
            return PaywallOutput.NotApplicable;
        }

        // Split command arguments (price and level/points)
        String[] args = ctx.text.attachPlaceholders(panel, pos, player, command.substring("xp-paywall=".length()).trim()).split(" ");
        if (args.length < 2) {
            ctx.text.sendString(player, ctx.tag + "Invalid xp-paywall usage. Not enough arguments.");
            return PaywallOutput.Blocked;
        }

        try {
            // Determine the player's balance (XP or level)
            int balance = 0;
            if (args[1].startsWith("level")) {
                balance = player.getLevel();
            } else {
                balance = getPlayerExp(player);
            }

            int requiredAmount = Integer.parseInt(args[0]);

            if (balance >= requiredAmount) {
                if (args[1].startsWith("level")) {
                    if (performOperation) {
                        player.setLevel(player.getLevel() - requiredAmount);
                    }
                } else {
                    if (performOperation) {
                        removePlayerExp(player, requiredAmount);
                    }
                }

                // Send success message if enabled
                if (ctx.configHandler.isTrue("purchase.xp.enable") && performOperation) {
                    String successMsg = ctx.configHandler.config.getString("purchase.xp.success");
                    if (successMsg != null) {
                        ctx.text.sendString(panel, PanelPosition.Top, player,
                                successMsg.replaceAll("%cp-args%", args[0]));
                    }
                }

                return PaywallOutput.Passed;
            } else {
                // Send failure message if enabled
                if (ctx.configHandler.isTrue("purchase.xp.enable")) {
                    String failureMsg = ctx.configHandler.config.getString("purchase.xp.failure");
                    if (failureMsg != null) {
                        ctx.text.sendString(panel, PanelPosition.Top, player, failureMsg);
                    }
                }
                return PaywallOutput.Blocked;
            }
        } catch (Exception ex) {
            ctx.debug.send(ex, player, ctx);
            ctx.text.sendString(player, ctx.tag + ctx.configHandler.config.getString("config.format.error")
                    + " command: xp-paywall");
            return PaywallOutput.Blocked;
        }
    }

    // Experience math is a bit doggy doo doo so these will help to calculate values
    // Calculate total experience up to a level

    // @author thelonelywolf@https://github.com/TheLonelyWolf1
    private int getExpAtLevel(int level) {
        if (level <= 16) {
            return (int) (Math.pow(level, 2) + 6 * level);
        } else if (level <= 31) {
            return (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360.0);
        } else {
            return (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220.0);
        }
    }

    // Calculate amount of EXP needed to level up
    private int getExpToLevelUp(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    // Calculate player's current EXP amount
    private int getPlayerExp(Player player) {
        int exp = 0;
        int level = player.getLevel();

        // Get the amount of XP in past levels
        exp += getExpAtLevel(level);

        // Get amount of XP towards next level
        exp += Math.round(getExpToLevelUp(level) * player.getExp());

        return exp;
    }

    // Take EXP
    private int removePlayerExp(Player player, int exp) {
        // Get player's current exp
        int currentExp = getPlayerExp(player);

        // Reset player's current exp to 0
        player.setExp(0);
        player.setLevel(0);

        // Give the player their exp back, with the difference
        int newExp = currentExp - exp;
        player.giveExp(newExp);

        // Return the player's new exp amount
        return newExp;
    }
}