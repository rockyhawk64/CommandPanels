package me.rockyhawk.commandpanels.builder.logic;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.entity.Player;

public interface ConditionNode {
    boolean evaluate(Player player, Panel panel, Context ctx);
}
