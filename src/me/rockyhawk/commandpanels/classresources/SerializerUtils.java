package me.rockyhawk.commandpanels.classresources;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.stream.Collectors;

public class SerializerUtils {

    static LegacyComponentSerializer legacyComponentSerializer;

    //public static Component serializeText(String msg){
    //    return MiniMessage.miniMessage().deserialize(msg);
    //}

    public static boolean isPaperServer(){
        try {
            // Check for a class that exists only in Paper
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            // The class was not found, indicating a non-Paper server
            return false;
        }
    }

    public static Component doMiniMessage(String string) {
        legacyComponentSerializer = LegacyComponentSerializer.builder().hexColors().character('&').build();
        Component component = legacyComponentSerializer.deserialize(string.replace('§', '&'));

        return MiniMessage.miniMessage().deserialize(MiniMessage.miniMessage().serialize(component.decoration(TextDecoration.ITALIC, false))
                .replace("\\<", "<").replace("\\", "").replace("\n", "<br>")).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> doMiniMessage(List<String> strings) {
        return strings.stream()
                .map(SerializerUtils::doMiniMessage)
                .collect(Collectors.toList());
    }

    public static String doMiniMessageLegacy(String string) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        Component component = miniMessage.deserialize(string);

        Bukkit.getLogger().info("Component: " + component);

        String legacyText = LegacyComponentSerializer.builder()
                .character('&')
                .hexColors()
                .build()
                .serialize(component);

        Bukkit.getLogger().info("Legacy: " + legacyText);

        Bukkit.getLogger().info("Convert: " + convertHexCodes(legacyText));

        return convertHexCodes(legacyText);
    }

    public static List<String> doMiniMessageLegacy(List<String> strings) {
        return strings.stream()
                .map(SerializerUtils::doMiniMessageLegacy)
                .collect(Collectors.toList());
    }

    // Helper method to convert &x&F&F&F&F&F&F to &#FFFFFF format
    private static String convertHexCodes(String text) {
        StringBuilder result = new StringBuilder();
        char[] chars = text.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            // Check for the start of a hex color code (&x pattern)
            if (chars[i] == '&' && i + 1 < chars.length && chars[i + 1] == 'x' && i + 13 <= chars.length) {
                // Found a hex color code
                StringBuilder hexCode = new StringBuilder("&#");
                for (int j = 2; j < 14; j += 2) {
                    hexCode.append(chars[i + j]);
                }
                result.append(hexCode.toString());
                i += 13; // Skip the next 12 characters (full hex code)
            } else {
                result.append(chars[i]);
            }
        }

        return result.toString();
    }
}
