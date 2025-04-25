package me.rockyhawk.commandpanels.interaction.commands.tags;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class BungeeTag implements TagResolver {
    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        String[] args = command.split("\\s+");  // Arguments are space-separated
        if(command.startsWith("force-server=")){
            //this contacts bungee and tells it to send the server change command without checking permissions
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(args[1]);
            player.sendPluginMessage(ctx.plugin, "BungeeCord", out.toByteArray());
            return true;
        } else if(command.startsWith("server=")){
            if(args.length >= 3){
                //This uses custom permission: server= servername permission
                if(player.hasPermission(args[2])){
                    //this contacts bungee and tells it to send the server change command whilst checking for CUSTOM permissions
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Connect");
                    out.writeUTF(args[1]);
                    player.sendPluginMessage(ctx.plugin, "BungeeCord", out.toByteArray());
                }
            }else if (player.hasPermission("bungeecord.command.server." + args[1].toLowerCase())) {
                //this contacts bungee and tells it to send the server change command whilst checking for permissions
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(args[1]);
                player.sendPluginMessage(ctx.plugin, "BungeeCord", out.toByteArray());
            }else{
                player.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
            }
            return true;
        }
        return false;
    }
}
