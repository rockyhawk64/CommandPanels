package me.rockyhawk.commandpanels.session;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public record CommandActions(
        List<String> requirements,
        List<String> commands,
        List<String> fail
) {
    public static CommandActions fromSection(ConfigurationSection section) {
        if (section == null) return new CommandActions(List.of(), List.of(), List.of());
        return new CommandActions(
                section.getStringList("requirements"),
                section.getStringList("commands"),
                section.getStringList("fail")
        );
    }
}