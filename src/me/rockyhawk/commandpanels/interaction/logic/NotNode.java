package me.rockyhawk.commandpanels.interaction.logic;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.entity.Player;

public class NotNode implements ConditionNode {
    private final ConditionNode child;

    public NotNode(ConditionNode child) {
        this.child = child;
    }

    @Override
    public boolean evaluate(Player player, Context ctx, Panel panel) {
        return !child.evaluate(player, ctx, panel);
    }
}

