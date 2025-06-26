package me.rockyhawk.commandpanels.interaction.logic;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.entity.Player;

import java.util.List;

public class LogicalNode implements ConditionNode {
    private final String operator; // "$AND" or "$OR"
    private final List<ConditionNode> conditions;

    public LogicalNode(String operator, List<ConditionNode> conditions) {
        this.operator = operator;
        this.conditions = conditions;
    }

    @Override
    public boolean evaluate(Player player, Context ctx, Panel panel) {
        switch (operator) {
            case "$AND":
                return conditions.stream().allMatch(cond -> cond.evaluate(player, ctx, panel));
            case "$OR":
                return conditions.stream().anyMatch(cond -> cond.evaluate(player, ctx, panel));
            default:
                return false;
        }
    }
}
