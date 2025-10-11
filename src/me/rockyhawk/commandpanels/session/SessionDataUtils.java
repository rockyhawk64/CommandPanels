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

    /**
     * On player Join and Leave,
     * Remove Session data and do an async save of the data file
     */
    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e) {
        removeSessionData(e.getPlayer());
        ctx.dataLoader.saveDataFileAsync();
    }

    @EventHandler
    public void onQuitEvent(PlayerQuitEvent e) {
        removeSessionData(e.getPlayer());
        ctx.dataLoader.saveDataFileAsync();
    }

    private void removeSessionData(Player p){
        p.getPersistentDataContainer().getKeys().stream()
                .filter(key -> key.getNamespace().equalsIgnoreCase("commandpanels"))
                .forEach(key -> p.getPersistentDataContainer().remove(key));
    }
}
