package me.rockyhawk.commandpanels.interaction.commands.paywalls;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.PaywallEvent;
import me.rockyhawk.commandpanels.interaction.commands.PaywallOutput;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class XpPaywall implements Listener {
    Context ctx;
    public XpPaywall(Context pl) {
        this.ctx = pl;
    }

    @EventHandler
    public void commandTag(PaywallEvent e){
        if(e.name.equalsIgnoreCase("xp-paywall=")){
            //if player uses xp-paywall= <price> <level:points>
            try {
                int balance;
                if (e.args[1].startsWith("level")) {
                    balance = e.p.getLevel();
                } else {
                    balance = getPlayerExp(e.p);
                }
                if (balance >= Integer.parseInt(e.args[0])) {
                    if (e.args[1].startsWith("level")) {
                        if (e.doDelete) e.p.setLevel(e.p.getLevel() - Integer.parseInt(e.args[0]));
                    } else {
                        if (e.doDelete) removePlayerExp(e.p, Integer.parseInt(e.args[0]));
                    }
                    //if the message is empty don't send
                    if (ctx.configHandler.isTrue("purchase.xp.enable") && e.doDelete) {
                        ctx.text.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.xp.success")).replaceAll("%cp-args%", e.args[0]));
                    }
                    e.PAYWALL_OUTPUT = PaywallOutput.Passed;
                } else {
                    if (ctx.configHandler.isTrue("purchase.xp.enable")) {
                        ctx.text.sendString(e.panel, PanelPosition.Top, e.p, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.xp.failure")));
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

    //Experience math is a bit doggy doo doo so these will help to calculate values
    // Calculate total experience up to a level

    // @author thelonelywolf@https://github.com/TheLonelyWolf1
    // @date 06 August 2021
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
