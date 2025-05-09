package me.rockyhawk.commandpanels.formatter.data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class DataManager implements Listener {
    //will return UUID if found or null
    public UUID getOffline(String playerName){
        return knownPlayers.getOrDefault(playerName, null);
    }

    //Run on initialisation
    public DataManager() {
        reloadAllPlayers();
    }

    //Bukkit.getOfflinePlayer uses MojangAPI and can be very slow if a player has never joined the server before
    //Will get all players who have ever joined the server before and use them
    private HashMap<String, UUID> knownPlayers = new HashMap<>();

    public void reloadAllPlayers(){
        knownPlayers.clear();
        try {
            for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                if (p.getName() != null) {
                    knownPlayers.put(p.getName(), p.getUniqueId());
                }

            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[CommandPanels] Skipping player retrieval; potential corrupt data.");
        } // Potential corrupt data, skip
    }

    //Add players who have joined the server to known players
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        knownPlayers.put(e.getPlayer().getName(), e.getPlayer().getUniqueId());
    }
}