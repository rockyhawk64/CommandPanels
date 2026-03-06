package me.rockyhawk.commandpanels.session;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class PanelCommandSettings {
    private final String command;
    private final List<String> aliases;
    private final boolean requireOnlinePlayer;
    private final String noArgDefault;

    public PanelCommandSettings(YamlConfiguration config) {
        this.command = config.getString("command", "");
        this.aliases = config.getStringList("aliases");
        this.requireOnlinePlayer = config.getBoolean("require-online-player", false);
        this.noArgDefault = config.getString("no-arg-default", "");
    }

    public String getCommand() {
        return command;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean requiresOnlinePlayer() {
        return requireOnlinePlayer;
    }

    public String getNoArgDefault() {
        return noArgDefault;
    }
}
