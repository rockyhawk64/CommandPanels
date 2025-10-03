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
        // Preserve RGB when serializing to legacy
        return LegacyComponentSerializer.builder()
                .hexColors()                              // enable hex serialization
                .useUnusualXRepeatedCharacterHexFormat()  // §x§R§R§G§G§B§B form
                .build()
                .serialize(component);
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
     * Below includes compatibility with legacy codes
     * Handles:
     *  - Mojang RGB form: §x§R§R§G§G§B§B (and &x&R&R&G&G&B&B)
     *  - Simple hex: &#[0-9a-f]{6} or §#[0-9a-f]{6}
     *  - Simple legacy codes: &a, §l, etc.
     */
    private String convertLegacyToMiniMessage(String input) {
        if (input == null) return "";

        String processed = input;

        // Replace Mojang "§x§R§R§G§G§B§B" (or &x...) with MiniMessage <#RRGGBB>
        Matcher mojangMatcher = MOJANG_HEX.matcher(processed);
        StringBuilder mmSB = new StringBuilder();
        while (mojangMatcher.find()) {
            String hex =
                    (mojangMatcher.group(1) +
                            mojangMatcher.group(2) +
                            mojangMatcher.group(3) +
                            mojangMatcher.group(4) +
                            mojangMatcher.group(5) +
                            mojangMatcher.group(6)).toLowerCase();
            mojangMatcher.appendReplacement(mmSB, Matcher.quoteReplacement("<#" + hex + ">"));
        }
        mojangMatcher.appendTail(mmSB);
        processed = mmSB.toString();

        // Replace &#[0-9a-f]{6} / §#[0-9a-f]{6} with <#RRGGBB>
        Matcher hexMatcher = LEGACY_HASH_HEX.matcher(processed);
        StringBuilder sb1 = new StringBuilder();
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1).toLowerCase();
            hexMatcher.appendReplacement(sb1, Matcher.quoteReplacement("<#" + hex + ">"));
        }
        hexMatcher.appendTail(sb1);
        processed = sb1.toString();

        // Replace simple legacy codes (&a, §l, etc.) with MiniMessage tags
        Matcher simpleMatcher = LEGACY_SIMPLE.matcher(processed);
        StringBuilder sb2 = new StringBuilder();
        while (simpleMatcher.find()) {
            char code = Character.toLowerCase(simpleMatcher.group(1).charAt(0));
            String replacement = COLOR_MAP.getOrDefault(code, "reset");
            simpleMatcher.appendReplacement(sb2, Matcher.quoteReplacement("<" + replacement + ">"));
        }
        simpleMatcher.appendTail(sb2);

        return sb2.toString();
    }

    // Patterns for legacy color codes
    // Mojang RGB: §x§R§R§G§G§B§B or &x&R&R&G&G&B&B
    private final Pattern MOJANG_HEX = Pattern.compile(
            "(?i)[&§]x" +
                    "[&§]([0-9a-f])" +
                    "[&§]([0-9a-f])" +
                    "[&§]([0-9a-f])" +
                    "[&§]([0-9a-f])" +
                    "[&§]([0-9a-f])" +
                    "[&§]([0-9a-f])"
    );

    // Pattern for legacy simple hex &#[0-9a-f]{6} or §#[0-9a-f]{6}
    private final Pattern LEGACY_HASH_HEX = Pattern.compile("(?i)[&§]#([0-9a-f]{6})");

    // Single-char legacy codes
    private final Pattern LEGACY_SIMPLE = Pattern.compile("(?i)[&§]([0-9a-fk-or])");

    // Map of legacy codes -> MiniMessage tags
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
