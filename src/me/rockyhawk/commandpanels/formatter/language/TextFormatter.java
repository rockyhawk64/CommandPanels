package me.rockyhawk.commandpanels.formatter.language;

import me.clip.placeholderapi.PlaceholderAPI;
import me.rockyhawk.commandpanels.Context;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormatter {
    public final LanguageManager lang;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public TextFormatter(Context ctx) {
        this.lang = new LanguageManager(ctx);
    }

    public TextComponent getPrefix() {
        String translatedPrefix = lang.translate(Message.PREFIX) + " ";
        if (Objects.equals(translatedPrefix, "[CommandPanels] ")) {
            return Component.text("[", NamedTextColor.GOLD)
                    .append(Component.text("CommandPanels", NamedTextColor.YELLOW))
                    .append(Component.text("] ", NamedTextColor.GOLD));
        } else {
            return (TextComponent) deserializeAppropriately(translatedPrefix);
        }
    }

    private Component buildLocalizedComponent(String translatedMessage, NamedTextColor defaultColor) {
        if (translatedMessage == null || translatedMessage.isEmpty()) return Component.empty();

        Component parsed = deserializeAppropriately(translatedMessage);

        return Component.text()
                .color(defaultColor)
                .append(parsed)
                .build();
    }

    public void sendInfo(Audience audience, Message message, Object... args) {
        String translated = lang.translate(message, args);
        Component comp = buildLocalizedComponent(translated, NamedTextColor.WHITE);
        audience.sendMessage(getPrefix().append(comp));
    }

    public void sendWarn(Audience audience, Message message, Object... args) {
        String translated = lang.translate(message, args);
        Component comp = buildLocalizedComponent(translated, NamedTextColor.YELLOW);
        audience.sendMessage(getPrefix().append(comp));
    }

    public void sendError(Audience audience, Message message, Object... args) {
        String translated = lang.translate(message, args);
        Component comp = buildLocalizedComponent(translated, NamedTextColor.RED);
        audience.sendMessage(getPrefix().append(comp));
    }

    public void sendHelp(Audience audience, Message command, Message description, Object... args) {
        String translatedCommand = lang.translate(command, args);
        String translatedDescription = lang.translate(description, args);

        Component cmdComp = buildLocalizedComponent(translatedCommand, NamedTextColor.GOLD);
        Component descComp = buildLocalizedComponent(translatedDescription, NamedTextColor.WHITE);

        Component space = Component.text(" ").color(NamedTextColor.WHITE);

        Component helpComp = Component.text()
                .append(cmdComp)
                .append(space)
                .append(descComp)
                .build();

        audience.sendMessage(helpComp);
    }

    @NotNull
    public Component parseTextToComponent(Player player, String input) {
        if (input == null || input.isEmpty()) return Component.empty();

        input = applyPlaceholders(player, input);
        return deserializeAppropriately(input);
    }

    @Subst("")
    @NotNull
    public String parseTextToString(Player player, String input) {
        Component component = parseTextToComponent(player, input);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public String applyPlaceholders(Player player, String input) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
            return PlaceholderAPI.setPlaceholders(offlinePlayer, input);
        }
        return input;
    }

    private Component deserializeAppropriately(String input) {
        try {
            String normalized = convertLegacyToMiniMessage(input);
            Component mmComponent = miniMessage.deserialize(normalized);

            // If no explicit italic tag was used, disable italics by default
            if (mmComponent.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
                mmComponent = mmComponent.decoration(TextDecoration.ITALIC, false);
            }

            return mmComponent;
        } catch (Exception e) {
            return Component.text(input).decoration(TextDecoration.ITALIC, false);
        }
    }

    /**
     * For compatibility with legacy codes
     * This will convert anything legacy to MiniMessage
     * Once converted, everything will be parsed as pure MiniMessage text
     */
    private String convertLegacyToMiniMessage(String input) {
        if (input == null) return "";

        // Replace hex colors
        Matcher hexMatcher = LEGACY_HEX.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1);
            hexMatcher.appendReplacement(sb, "<#" + hex + ">");
        }
        hexMatcher.appendTail(sb);

        String processed = sb.toString();

        // Replace simple codes
        Matcher simpleMatcher = LEGACY_SIMPLE.matcher(processed);
        sb = new StringBuffer();
        while (simpleMatcher.find()) {
            char code = simpleMatcher.group(1).toLowerCase().charAt(0);
            String replacement = COLOR_MAP.getOrDefault(code, "reset");
            simpleMatcher.appendReplacement(sb, "<" + replacement + ">");
        }
        simpleMatcher.appendTail(sb);

        return sb.toString();
    }

    // Patterns for legacy color codes for compatibility
    private final Pattern LEGACY_HEX = Pattern.compile("(?i)[&§]#([0-9a-f]{6})"); // & or § followed by hex
    private final Pattern LEGACY_SIMPLE = Pattern.compile("(?i)[&§]([0-9a-fk-or])"); // single-char codes

    // Map of legacy codes for compatibility
    private final java.util.Map<Character, String> COLOR_MAP = java.util.Map.ofEntries(
            java.util.Map.entry('0', "black"),
            java.util.Map.entry('1', "dark_blue"),
            java.util.Map.entry('2', "dark_green"),
            java.util.Map.entry('3', "dark_aqua"),
            java.util.Map.entry('4', "dark_red"),
            java.util.Map.entry('5', "dark_purple"),
            java.util.Map.entry('6', "gold"),
            java.util.Map.entry('7', "gray"),
            java.util.Map.entry('8', "dark_gray"),
            java.util.Map.entry('9', "blue"),
            java.util.Map.entry('a', "green"),
            java.util.Map.entry('b', "aqua"),
            java.util.Map.entry('c', "red"),
            java.util.Map.entry('d', "light_purple"),
            java.util.Map.entry('e', "yellow"),
            java.util.Map.entry('f', "white"),
            java.util.Map.entry('k', "obfuscated"),
            java.util.Map.entry('l', "bold"),
            java.util.Map.entry('m', "strikethrough"),
            java.util.Map.entry('n', "underlined"),
            java.util.Map.entry('o', "italic"),
            java.util.Map.entry('r', "reset")
    );
}
