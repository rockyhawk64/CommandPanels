package me.rockyhawk.commandpanels.session;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.logic.ConditionNode;
import me.rockyhawk.commandpanels.builder.logic.ConditionParser;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public abstract class Panel {
    private final String name;
    private final String title;
    private final String conditions;
    private final String command; // Command used to open the panel
    private final List<String> aliases; // Aliases for command that opens the panel
    private final List<String> commands; // Commands that run when panel is opened
    private final String type;

    public Panel(String name, YamlConfiguration config) {
        this.name = name;
        this.conditions = config.getString("conditions", "");
        this.title = config.getString("title", "Panel");
        this.command = config.getString("command", "");
        this.aliases = config.getStringList("aliases");
        this.commands = config.getStringList("commands");
        this.type = config.getString("type", "inventory");
    }

    public boolean canOpen(Player player, Context ctx) {
        // Check the panel condition
        if (this.conditions.trim().isEmpty()) return true;
        try {
            ConditionNode node = new ConditionParser().parse(this.conditions);
            return node.evaluate(player, ctx);
        } catch (Exception e) {
            return false;
        }
    }

    // Updates data to set the current panel and previous panel.
    public void updatePanelData(Context ctx, Player p) {
        NamespacedKey keyCurrent = new NamespacedKey(ctx.plugin, "current");
        NamespacedKey keyPrevious = new NamespacedKey(ctx.plugin, "previous");
        PersistentDataContainer container = p.getPersistentDataContainer();

        // Move current → previous
        String current = container.get(keyCurrent, PersistentDataType.STRING);
        current = (current != null) ? current : "";
        container.set(keyPrevious, PersistentDataType.STRING, current);

        // Set this panel as the new current
        container.set(keyCurrent, PersistentDataType.STRING, this.name);
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

    public List<String> getCommands() {
        return commands;
    }

    public String getTitle() {
        return title;
    }
}