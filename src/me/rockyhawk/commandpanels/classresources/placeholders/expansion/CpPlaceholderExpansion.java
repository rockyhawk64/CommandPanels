package me.rockyhawk.commandpanels.classresources.placeholders.expansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CpPlaceholderExpansion extends PlaceholderExpansion {

    private final CommandPanels plugin;

    public CpPlaceholderExpansion(CommandPanels plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getAuthor() {
        return "RockyHawk";
    }

    @Override
    public String getIdentifier() {
        return "commandpanels";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    /*
    external use only, not to be used for example inside a panel
    usage: %commandpanels_<placeholder>%
    so for example %cp-data-test% instead you would do %commandpanels_data-test%
    */
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        return plugin.placeholders.cpPlaceholders(null, PanelPosition.Top, (Player)player, identifier);
    }
}
