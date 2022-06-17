package me.rockyhawk.commandpanels.classresources;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class SerializerUtils {

    public static Component serializeText(String msg){
        Component parsedText = MiniMessage.miniMessage().deserialize(msg);
        return parsedText;
    }
}
