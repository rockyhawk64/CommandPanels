package me.rockyhawk.commandpanels.formatter.placeholders;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.resolvers.*;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        resolvers.add(new CheckInventoryPlaceholder());
        resolvers.add(new DamagedPlaceholder());
        resolvers.add(new DataPlaceholder());
        resolvers.add(new Identical());
        resolvers.add(new LorePlaceholder());
        resolvers.add(new MaterialPlaceholder());
        resolvers.add(new MathDataPlaceholder());
        resolvers.add(new ModelDataPlaceholder());
        resolvers.add(new NamePlaceholder());
        resolvers.add(new NBTPlaceholder());
        resolvers.add(new CustomPlaceholders());
        resolvers.add(new PlayerPlaceholders());
        resolvers.add(new PlayerOnlinePlaceholder());
        resolvers.add(new PotionPlaceholder());
        resolvers.add(new RandomPlaceholder());
        resolvers.add(new ServerPlaceholder());
        resolvers.add(new SetDataPlaceholder());
        resolvers.add(new StackPlaceholder());
        resolvers.add(new UUIDPlaceholder());

    }

    // Parse placeholders in a string with this
    public String setPlaceholders(Panel panel, PanelPosition position, org.bukkit.entity.Player p, String str, boolean primary) {
        String[] HOLDERS = getPlaceholderEnds(panel, primary);
        if (HOLDERS[0] == null || HOLDERS[1] == null) return str;

        // Escape the start/end symbols for regex
        String start = Pattern.quote(HOLDERS[0]);
        String end = Pattern.quote(HOLDERS[1]);
        Pattern pattern = Pattern.compile(start + "cp-([a-zA-Z0-9_\\\:\,-]+)" + end);

        int maxPasses = 255;
        int count = 0;

        String previous;
        do {
            previous = str;
            StringBuffer sb = new StringBuffer();
            Matcher matcher = pattern.matcher(str);

            while (matcher.find()) {
                String identifier = matcher.group(1);
                String replacement = resolvePlaceholder(panel, position, p, identifier);
                if (replacement == null) replacement = "";
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }

            matcher.appendTail(sb);
            str = sb.toString();
            count++;

        } while (!str.equals(previous) && count < maxPasses);

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
