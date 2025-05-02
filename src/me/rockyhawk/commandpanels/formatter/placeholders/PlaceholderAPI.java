package me.rockyhawk.commandpanels.formatter.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPI extends PlaceholderExpansion {

    private final Context ctx;

    public PlaceholderAPI(Context pl) {
        this.ctx = pl;
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
        return ctx.plugin.getDescription().getVersion();
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
        try {
            return ctx.placeholders.resolvePlaceholder(null, PanelPosition.Top, (Player) player, identifier);
        }catch (Exception e){
            return "null";
        }
    }
}