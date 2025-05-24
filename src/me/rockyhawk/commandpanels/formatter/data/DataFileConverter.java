package me.rockyhawk.commandpanels.formatter.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class DataFileConverter {
    private final DataLoader loader;

    public DataFileConverter(DataLoader dataLoader) {
        this.loader = dataLoader;
    }

    //This will convert data from CommandPanels 3.22.4 to newer versions if found
    public void convertOldProfileLayout() {
        ConfigurationSection oldProfiles = loader.dataConfig.getConfigurationSection("playerData");
        if (oldProfiles == null) return;

        for (String uuid : oldProfiles.getKeys(false)) {
            ConfigurationSection oldData = oldProfiles.getConfigurationSection(uuid);
            if (oldData == null) continue;

            // Resolve the player's name if they're online, leave null if not known
            String playerName = Optional.ofNullable(Bukkit.getPlayer(UUID.fromString(uuid)))
                    .map(Player::getName)
                    .orElse("Unknown"); // Or store null and populate on next login

            // Set the new format
            String basePath = "player_data.resolved_profiles." + uuid;
            loader.dataConfig.set(basePath + ".last_known_name", playerName);

            ConfigurationSection newDataSection = loader.dataConfig.createSection(basePath + ".data");
            for (String key : oldData.getKeys(false)) {
                Object value = oldData.get(key);
                newDataSection.set(key, value);
            }
        }

        // Remove the old top-level section
        loader.dataConfig.set("playerData", null);

        // Save the config after
        loader.saveDataFile();
    }
}