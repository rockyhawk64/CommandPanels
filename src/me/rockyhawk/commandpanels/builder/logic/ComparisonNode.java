package me.rockyhawk.commandpanels.builder.logic;

import me.rockyhawk.commandpanels.Context;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
        /*
        Do not parse placeholders of conditions before using this it will be handled internally
        Remove spaces from placeholders before parsing, so they can be compared with no spaces correctly
         */
        String parsedLeftRaw = ctx.text.parseTextToString(player, left).replaceAll("\\s+", "_");  // e.g., %player_balance% → "600"
        String parsedRightRaw = ctx.text.parseTextToString(player, right).replaceAll("\\s+", "_");

        /*
        parseTextToString will parse colour and placeholders
        After parsing strip colour, parsing and stripping will remove colour formatting
        */
        player.sendMessage("l:"+parsedLeftRaw);
        player.sendMessage("r:"+parsedRightRaw);
        String parsedLeft = toPlainText(parsedLeftRaw);
        String parsedRight = toPlainText(parsedRightRaw);

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

    public static String toPlainText(String input) {
        try {
            if (input.contains("§")) {
                Component legacyComp = LegacyComponentSerializer.legacySection().deserialize(input);
                return PlainTextComponentSerializer.plainText().serialize(legacyComp);
            } else {
                Component miniComp = MiniMessage.miniMessage().deserialize(input);
                return PlainTextComponentSerializer.plainText().serialize(miniComp);
            }
        } catch (Exception ignored) {}

        return input; // Fallback: return raw
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

