package me.rockyhawk.commandpanels.formatter.placeholders;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.resolvers.*;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;

import java.util.ArrayList;
import java.util.List;

public class Placeholders {
    private final Context ctx;
    private final List<PlaceholderResolver> resolvers = new ArrayList<>();

    public Placeholders(Context ctx) {
        this.ctx = ctx;
        loadPlaceholders();
    }

    // Other plugins can add placeholders using this
    public void addPlaceholder(PlaceholderResolver placeholder) {
        resolvers.add(placeholder);
    }

    // Loads built in placeholders
    private void loadPlaceholders() {
        resolvers.add(new CheckInventory());
        resolvers.add(new Damaged());
        resolvers.add(new Data());
        resolvers.add(new Identical());
        resolvers.add(new Lore());
        resolvers.add(new Material());
        resolvers.add(new MathData());
        resolvers.add(new ModelData());
        resolvers.add(new Name());
        resolvers.add(new NBT());
        resolvers.add(new PanelSpecific());
        resolvers.add(new Player());
        resolvers.add(new PlayerOnline());
        resolvers.add(new Potion());
        resolvers.add(new Random());
        resolvers.add(new Server());
        resolvers.add(new SetData());
        resolvers.add(new Stack());
        resolvers.add(new UUID());

    }

    // Parse placeholders in a string with this
    public String setPlaceholders(Panel panel, PanelPosition position, org.bukkit.entity.Player p, String str, boolean primary) {
        String[] HOLDERS = getPlaceholderEnds(panel, primary);
        while (str.contains(HOLDERS[0] + "cp-")) {
            try {
                int start = str.indexOf(HOLDERS[0] + "cp-");
                int end = str.indexOf(HOLDERS[1], start + 1);
                String identifier = str.substring(start, end).replace(HOLDERS[0] + "cp-", "").replace(HOLDERS[1], "");
                String value = resolvePlaceholder(panel, position, p, identifier);
                str = str.replace(str.substring(start, end) + HOLDERS[1], value);
            } catch (Exception ex) {
                ctx.debug.send(ex, p, ctx);
                break;
            }
        }
        return str;
    }

    protected String resolvePlaceholder(Panel panel, PanelPosition position, org.bukkit.entity.Player p, String identifier) {
        for (PlaceholderResolver resolver : resolvers) {
            if (resolver.canResolve(identifier)) {
                try {
                    identifier = resolver.resolve(panel, position, p, identifier, ctx);
                } catch (Exception ex) {
                    ctx.debug.send(ex, p, ctx);
                }
            }
        }
        return identifier;
    }

    public String[] getPlaceholderEnds(Panel panel, boolean primary) {
        String[] primaryEnds = {
                ctx.configHandler.config.getString("placeholders.primary.start"),
                ctx.configHandler.config.getString("placeholders.primary.end")
        };
        String[] secondaryEnds = {
                ctx.configHandler.config.getString("placeholders.secondary.start"),
                ctx.configHandler.config.getString("placeholders.secondary.end")
        };
        if (panel != null && panel.getConfig().isSet("placeholders")) {
            if (panel.getConfig().isSet("placeholders.primary")) {
                primaryEnds = new String[]{
                        panel.getConfig().getString("placeholders.primary.start"),
                        panel.getConfig().getString("placeholders.primary.end")
                };
            }
            if (panel.getConfig().isSet("placeholders.secondary")) {
                secondaryEnds = new String[]{
                        panel.getConfig().getString("placeholders.secondary.start"),
                        panel.getConfig().getString("placeholders.secondary.end")
                };
            }
        }
        return primary ? primaryEnds : secondaryEnds;
    }

}
