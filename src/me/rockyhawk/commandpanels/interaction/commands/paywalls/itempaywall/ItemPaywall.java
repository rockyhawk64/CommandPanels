package me.rockyhawk.commandpanels.interaction.commands.paywalls.itempaywall;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.PaywallOutput;
import me.rockyhawk.commandpanels.interaction.commands.PaywallResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ItemPaywall implements PaywallResolver {

    private final ItemRemovalHandler itemRemovalHandler;

    public ItemPaywall() {
        this.itemRemovalHandler = new ItemRemovalHandler();
    }

    @Override
    public PaywallOutput handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command, boolean performOperation) {
        if (!command.toLowerCase().startsWith("item-paywall=")) {
            return PaywallOutput.NotApplicable;
        }

        try {
            String[] args = command.substring("item-paywall=".length()).trim().split(" ");
            if (args.length < 2) {
                ctx.text.sendString(player, ctx.tag + "Invalid item-paywall usage. Not enough arguments.");
                return PaywallOutput.Blocked;
            }

            boolean ignoreNBT = false;
            for (String val : args) {
                if (val.equals("IGNORENBT")) {
                    ignoreNBT = true;
                    break;
                }
            }

            ItemValidator itemValidator = new ItemValidator(ctx);
            ItemStack sellItem = itemValidator.createItemFromArgs(args, panel, pos, player);

            // Try to remove the item and determine outcome
            PaywallOutput removedItem = PaywallOutput.Blocked;
            if (itemRemovalHandler.removeItem(ctx, player, sellItem, ignoreNBT, performOperation)) {
                removedItem = PaywallOutput.Passed;
            }

            // Send message and return
            if (removedItem == PaywallOutput.Blocked) {
                if (ctx.configHandler.isTrue("purchase.item.enable")) {
                    ctx.text.sendString(panel, pos, player, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.item.failure")));
                }
            } else {
                if (ctx.configHandler.isTrue("purchase.item.enable") && performOperation) {
                    ctx.text.sendString(panel, pos, player, Objects.requireNonNull(ctx.configHandler.config.getString("purchase.item.success")).replaceAll("%cp-args%", args[0]));
                }
            }

            return removedItem;
        } catch (Exception e) {
            ctx.debug.send(e, player, ctx);
            ctx.text.sendString(player, ctx.tag + ctx.configHandler.config.getString("config.format.error") + " command: item-paywall");
            return PaywallOutput.Blocked;
        }
    }
}