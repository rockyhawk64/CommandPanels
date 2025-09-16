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

public class TextFormatter {
    public final LanguageManager lang;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();

    // On plugin load the tag can use a custom name from the language file
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

    /**
     * Messaging helpers, support translation from the language file
     * Translate -> apply placeholders -> deserialize -> apply default color as a base.
     * Keeps inline colours from the message (they will override the base color).
     */
    private Component buildLocalizedComponent(String translatedMessage, NamedTextColor defaultColor) {
        if (translatedMessage == null || translatedMessage.isEmpty()) return Component.empty();

        // Parse to component
        Component parsed = deserializeAppropriately(translatedMessage);

        // Use a base component with the default color and append parsed message.
        return Component.text()
                .color(defaultColor)
                .append(parsed)
                .build();
    }

    /** Localized helpers using enum Message and the builder above. */
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

    /**
     * sendHelp: first part (command) gold, second part (description) white, space in between.
     */
    public void sendHelp(Audience audience, Message command, Message description, Object... args) {
        String translatedCommand = lang.translate(command, args);
        String translatedDescription = lang.translate(description, args);

        Component cmdComp = buildLocalizedComponent(translatedCommand, NamedTextColor.GOLD);
        Component descComp = buildLocalizedComponent(translatedDescription, NamedTextColor.WHITE);

        // Space between command and description — give it the same color as the description for consistency
        Component space = Component.text(" ").color(NamedTextColor.WHITE);

        Component helpComp = Component.text()
                .append(cmdComp)
                .append(space)
                .append(descComp)
                .build();

        audience.sendMessage(helpComp);
    }

    // Custom text parsers, does not support language translation
    @NotNull
    public Component parseTextToComponent(Player player, String input) {
        if (input == null || input.isEmpty()) return Component.empty();

        // Apply placeholders if PAPI is available
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
            Component mmComponent = miniMessage.deserialize(input);
            Component plain = Component.text(input);

            // If MiniMessage result is just plain text, it did nothing useful. fallback to legacy
            if (mmComponent.equals(plain)) {
                return legacySerializer.deserialize(input.replaceAll("(?i)&([0-9a-fk-or])", "§$1"))
                        .decoration(TextDecoration.ITALIC, false);
            }

            // Otherwise, MiniMessage worked. ensure non-italic if not explicitly set.
            if (mmComponent.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
                mmComponent = mmComponent.decoration(TextDecoration.ITALIC, false);
            }

            return mmComponent;
        } catch (Exception e) {
            // Totally broken input, fallback to plain non-italic
            return Component.text(input).decoration(TextDecoration.ITALIC, false);
        }
    }
}