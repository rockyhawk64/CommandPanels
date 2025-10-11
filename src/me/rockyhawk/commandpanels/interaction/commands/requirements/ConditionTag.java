package me.rockyhawk.commandpanels.interaction.commands.requirements;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.logic.ConditionNode;
import me.rockyhawk.commandpanels.builder.logic.ConditionParser;
import me.rockyhawk.commandpanels.interaction.commands.RequirementTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.entity.Player;

public class ConditionTag implements RequirementTagResolver {

    // Allows conditions to be used alternatively in requirements
    @Override
    public boolean isCorrectTag(String tag) {
        return tag.equalsIgnoreCase("[conditions]");
    }

    @Override
    public boolean check(Context ctx, Panel panel, Player player, String raw, String args) {
        return parseCondition(ctx, panel, player, args);
    }

    @Override
    public void execute(Context ctx, Panel panel, Player player, String raw, String args) {
    }

    private boolean parseCondition(Context ctx, Panel panel, Player player, String args) {
        if (!args.trim().isEmpty()) {
            ConditionNode conditionNode = new ConditionParser().parse(args);
            return conditionNode.evaluate(player, panel, ctx);
        }
        return false;
    }
}
