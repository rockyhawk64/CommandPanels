package me.rockyhawk.commandpanels.interaction.commands.requirements;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.RequirementTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.entity.Player;

public class DataTag implements RequirementTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.equalsIgnoreCase("[data]");
    }

    @Override
    public boolean check(Context ctx, Panel panel, Player player, String raw, String args) {
        String[] split = args.trim().split("\\s");
        if (split.length != 2) {
            ctx.text.sendError(player, "Invalid data requirement. Use: [data] <key> <amount>");
            return false;
        }

        String dataKey = split[0];
        double requiredAmount;
        try {
            requiredAmount = Double.parseDouble(split[1]);
        } catch (NumberFormatException e) {
            ctx.text.sendError(player, "Invalid amount.");
            return false;
        }

        double currentAmount;
        try {
            String valueStr = ctx.dataLoader.getUserData(player.getName(), dataKey);
            currentAmount = Double.parseDouble(valueStr);
        } catch (Exception e) {
            ctx.text.sendError(player, "Could not read data.");
            return false;
        }

        return currentAmount >= requiredAmount;
    }

    @Override
    public void execute(Context ctx, Panel panel, Player player, String raw, String args) {
        String[] split = args.trim().split("\\s");
        if (split.length != 2) return;

        String dataKey = split[0];
        double amount;
        try {
            amount = Double.parseDouble(split[1]);
        } catch (NumberFormatException e) {
            return;
        }

        // Subtract amount from player data
        ctx.dataLoader.doDataMath(player.getName(), dataKey, "-" + amount);
    }
}