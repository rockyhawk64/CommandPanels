package me.rockyhawk.commandpanels.classresources;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class SerializerUtils {

    public static Component serializeText(String msg){
        return MiniMessage.miniMessage().deserialize(msg);
    }
}
