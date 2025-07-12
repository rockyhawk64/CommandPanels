package me.rockyhawk.commandpanels.formatter;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.formatter.placeholders.DataPlaceholder;
import me.rockyhawk.commandpanels.formatter.placeholders.RandomPlaceholder;
import me.rockyhawk.commandpanels.formatter.placeholders.SessionDataPlaceholder;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;

public class Placeholders extends PlaceholderExpansion {
    private final Context ctx;
    private final List<PlaceholderResolver> resolvers = new ArrayList<>();

    public Placeholders(Context pl) {
        this.ctx = pl;
        loadPlaceholders();
    }

    private void loadPlaceholders(){
        resolvers.add(new SessionDataPlaceholder());
        resolvers.add(new DataPlaceholder());
        resolvers.add(new RandomPlaceholder());
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
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "";

        for (PlaceholderResolver resolver : resolvers) {
            try {
                String value = resolver.resolve(player, params, ctx);
                if(value != null){
                    return value;
                }
            } catch (Exception e) {
                return "unknown";
            }
        }
        return "unknown";
    }

}
