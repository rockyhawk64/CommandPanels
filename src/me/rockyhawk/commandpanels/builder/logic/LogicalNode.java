package me.rockyhawk.commandpanels.builder.logic;

import me.rockyhawk.commandpanels.Context;
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
    public boolean evaluate(Player player, Context ctx) {
        switch (operator) {
            case "$AND":
                return conditions.stream().allMatch(cond -> cond.evaluate(player, ctx));
            case "$OR":
                return conditions.stream().anyMatch(cond -> cond.evaluate(player, ctx));
            default:
                return false;
        }
    }
}
