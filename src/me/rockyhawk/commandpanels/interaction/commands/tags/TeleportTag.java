package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class TeleportTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[teleport]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        String[] args = ctx.text.parseTextToString(player, command).split("\\s+");

        float x, y, z, yaw = 0, pitch = 0;
        Player teleportedPlayer = player;
        World teleportedWorld = player.getWorld();

        try {
            x = Float.parseFloat(args[0]);
            y = Float.parseFloat(args[1]);
            z = Float.parseFloat(args[2]);
            for (String val : args) {
                if (val.startsWith("world=")) {
                    teleportedWorld = Bukkit.getWorld(val.substring(6));
                    continue;
                }
                if (val.startsWith("yaw=")) {
                    yaw = Float.parseFloat(val.substring(4));
                    continue;
                }
                if (val.startsWith("pitch=")) {
                    pitch = Float.parseFloat(val.substring(6));
                    continue;
                }
                if (val.startsWith("player=")) {
                    teleportedPlayer = Bukkit.getPlayer(val.substring(7));
                }
            }
            Location teleportLocation = new Location(teleportedWorld, x, y, z, yaw, pitch);
            teleportedPlayer.teleport(teleportLocation);
        } catch (Exception ex) {
            ctx.text.sendError(player, "Error with teleport tag");
        }
    }
}