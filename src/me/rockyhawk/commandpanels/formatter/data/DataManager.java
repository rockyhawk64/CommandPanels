package me.rockyhawk.commandpanels.formatter.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class DataManager implements Listener {
    private final DataLoader loader;

    //will return string for the location in the data config
    //RESOLVED profiles have a UUID and Player Name linked
    //UNRESOLVED profiles had data assigned before they were online to find a UUID
    public String getDataProfile(String playerName){
        Player player = Bukkit.getPlayer(playerName);

        // If there is an unresolved profile for this player
        boolean hasUnresolvedProfile = containsNoCase(loader.dataConfig, playerName);

        // Player is not null/exists on the server
        if(player != null){
            //Update name and creates resolved profile entry if not already there
            loader.dataConfig.set("player_data.resolved_profiles." + player.getUniqueId() + ".last_known_name", player.getName());

            //Merge unresolved to unresolved if unresolved profile was found
            if(hasUnresolvedProfile){
                mergeProfiles(player);
            }

            return "player_data.resolved_profiles." + player.getUniqueId();
        }

        // Use unresolved profile if player does not exist (creation of section is not necessary until data is written)
        return "player_data.unresolved_profiles." + playerName;
    }

    //Run on initialisation
    public DataManager(DataLoader dataLoader) {
        this.loader = dataLoader;
    }

    // Will get all player names that have data entries
    public List<String> getPlayerNames() {
        List<String> players = new ArrayList<>();

        ConfigurationSection unresolved = loader.dataConfig.getConfigurationSection("player_data.unresolved_profiles");
        ConfigurationSection resolved = loader.dataConfig.getConfigurationSection("player_data.resolved_profiles");

        // Collect all unresolved profile names (keys)
        if (unresolved != null) {
            players.addAll(unresolved.getKeys(false));
        }

        // Collect all resolved profile names from the last_known_name field
        if (resolved != null) {
            for (String uuidKey : resolved.getKeys(false)) {
                String path = "player_data.resolved_profiles." + uuidKey + ".last_known_name";
                String lastKnownName = loader.dataConfig.getString(path);
                if (lastKnownName != null && !lastKnownName.isEmpty()) {
                    players.add(lastKnownName);
                }
            }
        }

        return players;
    }

    //Same as contains but is not case-sensitive since Minecraft usernames are not case-sensitive
    private boolean containsNoCase(YamlConfiguration config, String targetKey) {
        ConfigurationSection section = config.getConfigurationSection("player_data.unresolved_profiles");
        if (section == null) return false;

        for (String key : section.getKeys(false)) {
            if (key.equalsIgnoreCase(targetKey)) {
                return true;
            }
        }
        return false;
    }

    private void mergeProfiles(Player player) {
        ConfigurationSection source = loader.dataConfig.getConfigurationSection("player_data.unresolved_profiles." + player.getName() + ".data");
        ConfigurationSection target = loader.dataConfig.getConfigurationSection("player_data.resolved_profiles." + player.getUniqueId() + ".data");

        if (source == null) return;

        if (target == null) {
            target = loader.dataConfig.createSection("player_data.resolved_profiles." + player.getUniqueId() + ".data");
        }

        for (String key : source.getKeys(false)) {
            Object sourceValue = source.get(key);
            // Overwrite or add the value in target
            target.set(key, sourceValue);
        }

        // Remove unresolved profile after merge
        loader.dataConfig.set("player_data.unresolved_profiles." + player.getName(), null);
    }
}