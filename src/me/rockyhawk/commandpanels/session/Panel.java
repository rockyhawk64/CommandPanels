package me.rockyhawk.commandpanels.session;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.logic.ConditionNode;
import me.rockyhawk.commandpanels.builder.logic.ConditionParser;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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

    public abstract void open(Context ctx, Player player, SessionManager.PanelOpenType openType);

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