package me.rockyhawk.commandpanels.formatter;

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

public class TextFormatter {
    public final LanguageManager lang;
    private final TextComponent tag;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();

    // On plugin load the tag can use a custom name from the language file
    public TextFormatter(Context ctx) {
        this.lang = new LanguageManager(ctx);
        this.tag = Component.text("[", NamedTextColor.GOLD)
                .append(Component.text(lang.translate("CommandPanels"), NamedTextColor.YELLOW))
                .append(Component.text("] ", NamedTextColor.GOLD));
    }

    // Messaging helpers, support translation from the language file
    public void sendError(Audience audience, String msg) {
        audience.sendMessage(tag.append(
                Component.text(
                        lang.translate(msg), NamedTextColor.RED)));
    }

    public void sendInfo(Audience audience, String msg) {
        audience.sendMessage(tag.append(
                Component.text(
                        lang.translate(msg), NamedTextColor.WHITE)));
    }

    public void sendWarn(Audience audience, String msg) {
        audience.sendMessage(tag.append(
                Component.text(
                        lang.translate(msg), NamedTextColor.YELLOW)));
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

    private String applyPlaceholders(Player player, String input) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            OfflinePlayer offp = Bukkit.getOfflinePlayer(player.getUniqueId());
            return PlaceholderAPI.setPlaceholders(offp, input);
        }
        return input;
    }

    private Component deserializeAppropriately(String input) {
        try {
            Component component;
            if (containsLegacyCodes(input)) {
                component = legacySerializer.deserialize(input.replaceAll("(?i)&([0-9a-fk-or])", "§$1"));
            } else {
                component = miniMessage.deserialize(input);
            }

            // Apply non-italic to root only if not already specified
            // By default Minecraft items are ITALIC
            if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
                component = component.decoration(TextDecoration.ITALIC, false);
            }

            return component;
        } catch (Exception e) {
            // Fallback plain non-italic text
            return Component.text(input).decoration(TextDecoration.ITALIC, false);
        }
    }

    // Check for legacy codes with regex
    private boolean containsLegacyCodes(String input) {
        return input.matches(".*&[0-9a-fk-or].*");
    }

    public TextComponent getTag() {
        return tag;
    }
}