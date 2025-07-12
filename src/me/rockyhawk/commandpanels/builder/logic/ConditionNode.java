package me.rockyhawk.commandpanels.builder.logic;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.entity.Player;

public interface ConditionNode {
    boolean evaluate(Player player, Context ctx);
}
