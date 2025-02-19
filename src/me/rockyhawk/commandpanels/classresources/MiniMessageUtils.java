package me.rockyhawk.commandpanels.classresources;

import me.rockyhawk.commandpanels.CommandPanels;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.stream.Collectors;

public class MiniMessageUtils {

    CommandPanels plugin;
    public MiniMessageUtils(CommandPanels pl) {
        this.plugin = pl;
    }

    /*
        MiniMessage is used here as an alternative to the standard Minecraft colour codes &
        As MiniMessage does not accept legacy colour codes at any point, which is
        a possibility when using Minecraft colour codes, running a try/catch allows for
        MiniMessage to be used anywhere and ignored when legacy colour codes are found.
     */

    public String doMiniMessageLegacy(String string) {
        MiniMessage miniMessage;
        try {
            // Try the newer method first
            miniMessage = (MiniMessage) MiniMessage.class.getMethod("miniMessage").invoke(null);
        } catch (Exception e) {
            try {
                // Fallback to older method
                miniMessage = (MiniMessage) MiniMessage.class.getMethod("get").invoke(null);
            } catch (Exception ex) {
                return string; // Return raw text if no method exists
            }
        }

        try {
            Component component = miniMessage.deserialize(string);
            return LegacyComponentSerializer.builder()
                    .character('&')
                    .hexColors()
                    .build()
                    .serialize(component);
        }catch (Exception e){
            return string;
        }
    }

    public List<String> doMiniMessageLegacy(List<String> strings) {
        return strings.stream()
                .map(this::doMiniMessageLegacy)
                .collect(Collectors.toList());
    }

    public Component doMiniMessage(String string) {
        LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.builder().hexColors().character('&').build();
        Component component = legacyComponentSerializer.deserialize(string.replace('§', '&'));

        return MiniMessage.miniMessage().deserialize(MiniMessage.miniMessage().serialize(component.decoration(TextDecoration.ITALIC, false))
                .replace("\\<", "<").replace("\\", "").replace("\n", "<br>")).decoration(TextDecoration.ITALIC, false);

    }

    public List<Component> doMiniMessage(List<String> strings) {
        return strings.stream()
                .map(this::doMiniMessage)
                .collect(Collectors.toList());
    }
}
