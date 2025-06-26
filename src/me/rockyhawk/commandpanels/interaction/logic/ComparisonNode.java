package me.rockyhawk.commandpanels.interaction.logic;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class ComparisonNode implements ConditionNode {
    private final String left;
    private final String operator;
    private final String right;

    public ComparisonNode(String left, String operator, String right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public boolean evaluate(Player player, Context ctx, Panel panel) {
        String parsedLeft = ctx.text.placeholders(panel, PanelPosition.Top, player, left);  // e.g., %player_balance% → "600"
        String parsedRight = ctx.text.placeholders(panel, PanelPosition.Top, player, right); // Just in case right has variables

        switch (operator) {
            case "$EQUALS":
                return parsedLeft.equalsIgnoreCase(parsedRight);
            case "$ATLEAST":
                try {
                    return Double.parseDouble(parsedLeft) >= Double.parseDouble(parsedRight);
                } catch (NumberFormatException e) {
                    return false;
                }
            case "$HASPERM":
                return player.hasPermission(parsedRight);
            default:
                return false;
        }
    }
}

