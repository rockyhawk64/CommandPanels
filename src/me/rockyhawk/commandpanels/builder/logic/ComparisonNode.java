package me.rockyhawk.commandpanels.builder.logic;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComparisonNode implements ConditionNode {
    private final String left;
    private final String operator;
    private final String right;

    // Legacy section-symbol codes, incl. each nibble of a hex color (§x§7§5§4...)
    private static final Pattern LEGACY_CODE = Pattern.compile("§[0-9A-FK-ORX]", Pattern.CASE_INSENSITIVE);

    private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");

    public ComparisonNode(String left, String operator, String right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public boolean evaluate(Player player, Panel panel, Context ctx) {
        String parsedLeft = stripColor(ctx.text.parseTextToString(player, left));
        String parsedRight = stripColor(ctx.text.parseTextToString(player, right));

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
                panel.getObserver().addPerm(parsedRight);
                if (p == null) return false;
                return p.hasPermission(parsedRight);
            default:
                return false;
        }
    }

    /**
     * parseTextToString always resolves color to legacy §-codes before returning,
     * so this only needs to strip that one representation.
     */
    public static String stripColor(String input) {
        if (input == null || input.indexOf('§') == -1) {
            return input; // fast path: no color codes present, skip regex entirely
        }
        return LEGACY_CODE.matcher(input).replaceAll("");
    }

    private Double extractNumber(String input) {
        if (input == null) return null;
        Matcher matcher = NUMBER.matcher(input);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        }
        return null;
    }
}
