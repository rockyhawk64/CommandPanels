package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class StopSoundTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.equalsIgnoreCase("[stopsound]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        String[] args = ctx.text.parseTextToString(player, command).split("\\s+");

        if (args.length == 0) {
            ctx.text.sendError(player, "No sound arguments provided.");
            return;
        }

        try {
            NamespacedKey key = parseKey(args[0].toLowerCase());
            Sound sound = Registry.SOUNDS.get(key);
            if (sound != null) {
                player.stopSound(sound);
            } else {
                player.stopSound(key.getKey());
            }
        } catch (Exception e) {
            ctx.text.sendError(player, "Failed to stop sound.");
        }
    }

    private NamespacedKey parseKey(String key) {
        return key.contains(":") ? NamespacedKey.fromString(key) : NamespacedKey.minecraft(key);
    }
}