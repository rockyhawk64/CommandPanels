package me.rockyhawk.commandpanels.builder.logic;

import me.rockyhawk.commandpanels.Context;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public boolean evaluate(Player player, Context ctx) {
        String parsedLeftRaw = ctx.text.parseTextToString(player, left);  // e.g., %player_balance% → "600"
        String parsedRightRaw = ctx.text.parseTextToString(player, right);

        /*
        parseTextToString will parse colour and placeholders
        After parsing strip colour, parsing and stripping will remove colour formatting
        */
        String parsedLeft = LegacyComponentSerializer.legacySection().deserialize(parsedLeftRaw).content();
        String parsedRight = LegacyComponentSerializer.legacySection().deserialize(parsedRightRaw).content();

        switch (operator) {
            case "$EQUALS":
                return parsedLeft.equalsIgnoreCase(parsedRight);
            case "$ATLEAST":
                Double leftValue = extractNumber(parsedLeft);
                Double rightValue = extractNumber(parsedRight);
                if (leftValue == null || rightValue == null) return false;
                return leftValue >= rightValue;
            case "$HASPERM":
                Player p = Bukkit.getPlayer(parsedLeft);
                if (p == null) return false;
                return p.hasPermission(parsedRight);
            default:
                return false;
        }
    }

    private Double extractNumber(String input) {
        if (input == null) return null;
        Matcher matcher = Pattern.compile("-?\\d+(\\.\\d+)?").matcher(input);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        }
        return null;
    }
}

