package me.rockyhawk.commandpanels.builder.logic;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.entity.Player;

public class NotNode implements ConditionNode {
    private final ConditionNode child;

    public NotNode(ConditionNode child) {
        this.child = child;
    }

    @Override
    public boolean evaluate(Player player, Context ctx) {
        return !child.evaluate(player, ctx);
    }
}

