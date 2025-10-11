package me.rockyhawk.commandpanels.builder.logic;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.entity.Player;

public class NotNode implements ConditionNode {
    private final ConditionNode child;

    public NotNode(ConditionNode child) {
        this.child = child;
    }

    @Override
    public boolean evaluate(Player player, Panel panel, Context ctx) {
        return !child.evaluate(player, panel, ctx);
    }
}

