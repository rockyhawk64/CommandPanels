package me.rockyhawk.commandpanels.session;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.logic.ConditionNode;
import me.rockyhawk.commandpanels.builder.logic.ConditionParser;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.logging.Level;

public abstract class Panel {
    private final String name;
    private final String title;
    private final String conditions;
    private final String command; // Command used to open the panel
    private final List<String> aliases; // Aliases for command that opens the panel
    private final CommandActions open; // Commands that run when panel is opened
    private final PanelObserver observer; // Values observed on the panel
    private final String type;

    public Panel(String name, YamlConfiguration config) {
        this.name = name;
        this.conditions = config.getString("conditions", "");
        this.title = config.getString("title", "Panel");
        this.command = config.getString("command", "");
        this.aliases = config.getStringList("aliases");
        this.type = config.getString("type", "inventory");

        observer = new PanelObserver(config);

        open = new CommandActions(
                config.getStringList("open.requirements"),
                config.getStringList("open.commands"),
                config.getStringList("open.fail")
        );

        // Run warnings for 4.3.0
        boolean dataWarning = false;
        boolean delayWarning = false;
        for (String key : config.getKeys(true)) {
            // For [data] overwrite
            if (!dataWarning && config.isList(key) && config.getStringList(key).stream()
                    .anyMatch(line -> line.contains("[data] overwrite"))){
                Bukkit.getLogger().log(Level.WARNING, "[CommandPanels] Change data tags to '[data] add', and '[data] set' in " + name + ".yml");
                dataWarning = true;
            }
            // For update-delay
            if(!delayWarning && key.endsWith("update-delay")) {
                Bukkit.getLogger().log(Level.WARNING, "[CommandPanels] Change update-delay to update-interval in " + name + ".yml");
                delayWarning = true;
            }
            if(dataWarning && delayWarning) break;
        }
    }

    // Check run for permission checks with commands
    public boolean passesConditions(Player player, Context ctx) {
        // Check the panel condition
        if (this.conditions.trim().isEmpty()) return true;
        try {
            ConditionNode node = new ConditionParser().parse(this.conditions);
            return node.evaluate(player, this, ctx);
        } catch (Exception e) {
            return false;
        }
    }

    // Checks for opening fresh panels
    public boolean canOpen(Player p, Context ctx) {
        // Do not open if user is in cooldown period
        NamespacedKey keyTime = new NamespacedKey(ctx.plugin, "last_open_time");
        Long lastOpenTime = p.getPersistentDataContainer().get(keyTime, PersistentDataType.LONG);
        long cooldownMillis = ctx.fileHandler.config.getLong("cooldown-ticks") * 50L;
        if (lastOpenTime != null && System.currentTimeMillis() - lastOpenTime < cooldownMillis) {
            ctx.text.sendError(p, Message.COOLDOWN_ERROR);
            return false;
        }

        // Do not allow the same panel to be opened again if already open
        return !(p.getOpenInventory().getTopInventory().getHolder() instanceof InventoryPanel panel)
                || !panel.getName().equals(getName());
    }

    // Updates data to set the current panel and previous panel.
    public void updatePanelData(Context ctx, Player p) {
        NamespacedKey keyCurrent = new NamespacedKey(ctx.plugin, "current");
        NamespacedKey keyPrevious = new NamespacedKey(ctx.plugin, "previous");
        NamespacedKey keyMillis = new NamespacedKey(ctx.plugin, "last_open_time");
        PersistentDataContainer container = p.getPersistentDataContainer();

        // Time the player last opened any panel
        container.set(keyMillis, PersistentDataType.LONG, System.currentTimeMillis());

        // Move current → previous
        String current = container.get(keyCurrent, PersistentDataType.STRING);
        current = (current != null) ? current : "";
        container.set(keyPrevious, PersistentDataType.STRING, current);

        // Set this panel as the new current
        container.set(keyCurrent, PersistentDataType.STRING, this.name);
        if(ctx.fileHandler.config.getBoolean("panel-snooper")){
            ctx.text.sendInfo(Bukkit.getConsoleSender(), Message.PANEL_OPEN_LOG, p.getName(), this.name);
        }
    }


    public abstract void open(Context ctx, Player player, boolean isNewPanelSession);

    public String getName() { return name; }

    public String getType() {
        return type;
    }

    public String getCommand() {
        return command;
    }
    public List<String> getAliases() {
        return aliases;
    }

    public CommandActions getOpenCommands() {
        return open;
    }

    public PanelObserver getObserver() {
        return observer;
    }

    public String getTitle() {
        return title;
    }
}