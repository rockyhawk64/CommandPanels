package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import com.earth2me.essentials.Essentials;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlayerOnlinePlaceholder implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("player-online-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String playerLocation = identifier.replace("player-online-", "");
        if (identifier.endsWith("-visible")){
            //for players that are visible only
            //remove -visible from the end of playerLocation
            playerLocation = playerLocation.replace("-visible", "");
            List<Player> playerList = new ArrayList<>();
            for(Player temp : Bukkit.getOnlinePlayers()) {
                if(!isPlayerVanished(temp)) {
                    playerList.add(temp);
                }
            }
            if(playerList.size() >= Integer.parseInt(playerLocation)){
                return playerList.get(Integer.parseInt(playerLocation)-1).getName();
            }
        } else {
            //for every player whether they are visible or not
            if(Bukkit.getOnlinePlayers().toArray().length >= Integer.parseInt(playerLocation)){
                return ((Player)Bukkit.getOnlinePlayers().toArray()[Integer.parseInt(playerLocation)-1]).getName();
            }
        }
        //player is not found
        return ctx.text.colour(Objects.requireNonNull(ctx.configHandler.config.getString("config.format.offline")));
    }

    private boolean isPlayerVanished(Player player) {
        //check if EssentialsX exists
        if(!Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            return false;
        }
        //check if player is vanished using essentials
        Essentials essentials = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");
        return essentials.getUser(player).isVanished();
    }
}