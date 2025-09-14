package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class SoundTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.equalsIgnoreCase("[sound]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        String[] args = ctx.text.parseTextToString(player, command).split("\\s+");

        if (args.length == 0) {
            ctx.text.sendError(player, Message.SOUND_NO_ARGS);
            return;
        }

        try {
            NamespacedKey key = parseKey(args[0].toLowerCase());
            float volume = 1f;
            float pitch = 1f;
            SoundCategory category = SoundCategory.MASTER;

            for (String arg : args) {
                if (arg.startsWith("volume=")) {
                    volume = Float.parseFloat(arg.substring(7));
                } else if (arg.startsWith("pitch=")) {
                    pitch = Float.parseFloat(arg.substring(6));
                } else if (arg.startsWith("category=")) {
                    try {
                        category = SoundCategory.valueOf(arg.substring(9).toUpperCase());
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            player.playSound(player.getLocation(), key.toString(), category, volume, pitch);

        } catch (Exception e) {
            ctx.text.sendError(player, Message.SOUND_PLAY_FAIL);
        }
    }

    private NamespacedKey parseKey(String key) {
        return key.contains(":") ? NamespacedKey.fromString(key) : NamespacedKey.minecraft(key);
    }
}