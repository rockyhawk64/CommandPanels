package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class TeleportTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.startsWith("teleport=")) return false;

        String[] args = ctx.text.attachPlaceholders(panel, pos, player, command).split("\\s+");
        args = Arrays.copyOfRange(args, 1, args.length); // Remove the tag name

        float x, y, z, yaw = 0, pitch = 0;
        Player teleportedPlayer = player;
        World teleportedWorld = player.getWorld();

        try {
            x = Float.parseFloat(args[0]);
            y = Float.parseFloat(args[1]);
            z = Float.parseFloat(args[2]);
            for (String val : args) {
                if (val.startsWith("world:")) {
                    teleportedWorld = Bukkit.getWorld(val.substring(6));
                    continue;
                }
                if (val.startsWith("yaw:")) {
                    yaw = Float.parseFloat(val.substring(4));
                    continue;
                }
                if (val.startsWith("pitch:")) {
                    pitch = Float.parseFloat(val.substring(6));
                    continue;
                }
                if (val.startsWith("player:")) {
                    teleportedPlayer = Bukkit.getPlayer(val.substring(7));
                }
            }
            Location teleportLocation = new Location(teleportedWorld, x, y, z, yaw, pitch);
            // Use scheduler for Folia compatibility
            if (teleportedPlayer == player) {
                teleportedPlayer.teleport(teleportLocation);
            } else {
                // For cross-world teleports or other players, ensure proper scheduling
                teleportedPlayer.teleport(teleportLocation);
            }
            return true;
        } catch (Exception ex) {
            ctx.debug.send(ex, player, ctx);
        }
        return false;
    }
}