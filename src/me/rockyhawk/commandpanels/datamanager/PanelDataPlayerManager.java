package me.rockyhawk.commandpanels.datamanager;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class PanelDataPlayerManager implements Listener {
    private CommandPanels plugin;

    public PanelDataPlayerManager(CommandPanels pl) {
        this.plugin = pl;
    }

    //will return UUID if found or null
    public UUID getOffline(String playerName){
        return knownPlayers.getOrDefault(playerName, null);
    }

    //Bukkit.getOfflinePlayer uses MojangAPI and can be very slow if a player has never joined the server before
    //Will get all players who have ever joined the server before and use them
    private HashMap<String, UUID> knownPlayers = new HashMap<>();
    public void reloadAllPlayers(){
        knownPlayers.clear();
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()){
            knownPlayers.put(p.getName(), p.getUniqueId());
        }
    }

    //Add players who have joined the server to known players
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        knownPlayers.put(e.getPlayer().getName(), e.getPlayer().getUniqueId());
    }
}
