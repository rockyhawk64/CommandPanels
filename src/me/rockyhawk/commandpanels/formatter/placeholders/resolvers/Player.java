package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import com.earth2me.essentials.Essentials;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;

public class Player implements PlaceholderResolver {
    @Override
    public boolean canResolve(String identifier) {
        return identifier.matches("^(player|online|panel|tag)-.*");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, org.bukkit.entity.Player p, String identifier, Context ctx) {
        switch(identifier){
            case("player-displayname"): {
                return p.getDisplayName();
            }
            case("player-name"): {
                return p.getName();
            }
            case("player-world"): {
                return p.getWorld().getName();
            }
            case("player-x"): {
                return String.valueOf(Math.round(p.getLocation().getX()));
            }
            case("player-y"): {
                return String.valueOf(Math.round(p.getLocation().getY()));
            }
            case("player-z"): {
                return String.valueOf(Math.round(p.getLocation().getZ()));
            }
            case("player-balance"): {
                try {
                    if (ctx.econ != null) {
                        return String.valueOf(Math.round(ctx.econ.getBalance(p)));
                    }
                } catch (Exception ignore) {
                    //skip
                }
            }
            case("online-players"): {
                return Integer.toString(Bukkit.getServer().getOnlinePlayers().size());
            }
            case("online-players-visible"): {
                //will not include players that are vanished
                int count = 0;
                for(org.bukkit.entity.Player temp : Bukkit.getOnlinePlayers()) {
                    if(!isPlayerVanished(temp)) {
                        count++;
                    }
                }
                return Integer.toString(count);
            }
            case("panel-position"): {
                return position.toString();
            }
            case("tag"): {
                return ctx.text.colour(ctx.tag);
            }
        }
        return identifier;
    }

    private boolean isPlayerVanished(org.bukkit.entity.Player player) {
        //check if EssentialsX exists
        if(!Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            return false;
        }
        //check if player is vanished using essentials
        Essentials essentials = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");
        return essentials.getUser(player).isVanished();
    }
}
