package me.rockyhawk.commandpanels.commands.subcommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.commands.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class HelpCommand implements SubCommand {

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return "commandpanels.command.help";
    }

    @Override
    public boolean execute(Context ctx, CommandSender sender, String[] args) {
        ctx.text.sendInfo(sender, "Plugin Commands:");

        if (sender.hasPermission("commandpanels.command.open")) {
            sender.sendMessage(Component.text("/pa open <panel> [player] ", NamedTextColor.GOLD)
                    .append(Component.text("Opens a panel", NamedTextColor.WHITE)));
        }

        if (sender.hasPermission("commandpanels.command.reload")) {
            sender.sendMessage(Component.text("/pa reload ", NamedTextColor.GOLD)
                    .append(Component.text("Reloads all config and panel files", NamedTextColor.WHITE)));
        }

        if (sender.hasPermission("commandpanels.command.generate")) {
            sender.sendMessage(Component.text("/pa generate ", NamedTextColor.GOLD)
                    .append(Component.text("Enter generate mode to generate panels", NamedTextColor.WHITE)));
        }

        if (sender.hasPermission("commandpanels.command.data")) {
            sender.sendMessage(Component.text("/pa data ", NamedTextColor.GOLD)
                    .append(Component.text("Modify data for players", NamedTextColor.WHITE)));
        }

        if (sender.hasPermission("commandpanels.command.version")) {
            sender.sendMessage(Component.text("/pa version ", NamedTextColor.GOLD)
                    .append(Component.text("Gets the plugin version", NamedTextColor.WHITE)));
        }

        if (sender.hasPermission("commandpanels.command.help")) {
            sender.sendMessage(Component.text("/pa help ", NamedTextColor.GOLD)
                    .append(Component.text("Shows this help menu", NamedTextColor.WHITE)));
        }

        // REMOVE AFTER CONVERTER IS REMOVED
        if (sender.hasPermission("commandpanels.command.convert")) {
            sender.sendMessage(Component.text("/pa convert ", NamedTextColor.GOLD)
                    .append(Component.text("Converts basic layout from v3 to v4 panels (not plug and play)", NamedTextColor.WHITE)));
        }

        return true;
    }
}
