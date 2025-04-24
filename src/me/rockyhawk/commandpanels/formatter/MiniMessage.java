package me.rockyhawk.commandpanels.formatter;

import me.rockyhawk.commandpanels.Context;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MiniMessage {

    Context ctx;
    public MiniMessage(Context pl) {
        this.ctx = pl;
    }

    /*
        MiniMessage is used here as an alternative to the standard Minecraft colour codes &
        As MiniMessage does not accept legacy colour codes at any point, which is
        a possibility when using Minecraft colour codes, running a try/catch allows for
        MiniMessage to be used anywhere and ignored when legacy colour codes are found.
     */

    public String doMiniMessageLegacy(String string) {
        net.kyori.adventure.text.minimessage.MiniMessage miniMessage;
        try {
            // Try the newer method first
            miniMessage = (net.kyori.adventure.text.minimessage.MiniMessage) net.kyori.adventure.text.minimessage.MiniMessage.class.getMethod("miniMessage").invoke(null);
        } catch (Exception e) {
            try {
                // Fallback to older method
                miniMessage = (net.kyori.adventure.text.minimessage.MiniMessage) net.kyori.adventure.text.minimessage.MiniMessage.class.getMethod("get").invoke(null);
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

    public Component doMiniMessage(String string) {
        LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.builder().hexColors().character('&').build();
        Component component = legacyComponentSerializer.deserialize(string.replace('§', '&'));

        return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(component.decoration(TextDecoration.ITALIC, false))
                .replace("\\<", "<").replace("\\", "").replace("\n", "<br>")).decoration(TextDecoration.ITALIC, false);

    }
}
