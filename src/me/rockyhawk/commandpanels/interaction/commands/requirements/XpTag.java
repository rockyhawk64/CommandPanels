package me.rockyhawk.commandpanels.interaction.commands.requirements;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.RequirementTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.entity.Player;

public class XpTag implements RequirementTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.equalsIgnoreCase("[xp]");
    }

    @Override
    public boolean check(Context ctx, Panel panel, Player player, String args) {
        String[] split = args.trim().split("\\s");
        if (split.length != 2) {
            ctx.text.sendError(player, "Invalid XP requirement. Use: [xp] <levels|points> <amount>");
            return false;
        }

        int amount;
        try {
            amount = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            ctx.text.sendError(player, "Invalid XP amount.");
            return false;
        }

        String type = split[0].toLowerCase();

        return switch (type) {
            case "levels" -> player.getLevel() >= amount;
            case "points" -> getPlayerExp(player) >= amount;
            default -> {
                ctx.text.sendError(player, "Invalid XP type.");
                yield false;
            }
        };
    }

    @Override
    public void execute(Context ctx, Panel panel, Player player, String args) {
        String[] split = args.trim().split("\\s");
        if (split.length != 2) return;

        int amount;
        try {
            amount = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            return;
        }

        String type = split[0].toLowerCase();

        switch (type) {
            case "levels" -> player.setLevel(player.getLevel() - amount);
            case "points" -> removePlayerExp(player, amount);
        }
    }

    private int getExpAtLevel(int level) {
        if (level <= 16) return level * level + 6 * level;
        else if (level <= 31) return (int) (2.5 * level * level - 40.5 * level + 360);
        else return (int) (4.5 * level * level - 162.5 * level + 2220);
    }

    private int getExpToLevelUp(int level) {
        if (level <= 15) return 2 * level + 7;
        else if (level <= 30) return 5 * level - 38;
        else return 9 * level - 158;
    }

    private int getPlayerExp(Player player) {
        int level = player.getLevel();
        float progress = player.getExp();
        return getExpAtLevel(level) + Math.round(getExpToLevelUp(level) * progress);
    }

    private void removePlayerExp(Player player, int exp) {
        int total = getPlayerExp(player);
        player.setExp(0);
        player.setLevel(0);
        player.giveExp(Math.max(0, total - exp));
    }
}