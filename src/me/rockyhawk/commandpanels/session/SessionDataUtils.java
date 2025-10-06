package me.rockyhawk.commandpanels.session;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SessionDataUtils implements Listener {

    private final Context ctx;

    public SessionDataUtils(Context ctx) {
        this.ctx = ctx;
    }

    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e) {
        removeSessionData(e.getPlayer());
    }

    @EventHandler
    public void onQuitEvent(PlayerQuitEvent e) {
        removeSessionData(e.getPlayer());
    }

    private void removeSessionData(Player p){
        p.getPersistentDataContainer().getKeys().stream()
                .filter(key -> key.getNamespace().equalsIgnoreCase("commandpanels"))
                .forEach(key -> p.getPersistentDataContainer().remove(key));
    }
}
