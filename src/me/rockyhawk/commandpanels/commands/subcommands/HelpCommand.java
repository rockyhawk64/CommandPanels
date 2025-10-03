package me.rockyhawk.commandpanels.commands.subcommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.commands.SubCommand;
import me.rockyhawk.commandpanels.formatter.language.Message;
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
        ctx.text.sendInfo(sender, Message.PLUGIN_COMMANDS);

        if (sender.hasPermission("commandpanels.command.open")) {
            ctx.text.sendHelp(sender, Message.HELP_OPEN_COMMAND, Message.HELP_OPEN_DESCRIPTION);
        }

        if (sender.hasPermission("commandpanels.command.reload")) {
            ctx.text.sendHelp(sender, Message.HELP_RELOAD_COMMAND, Message.HELP_RELOAD_DESCRIPTION);
        }

        if (sender.hasPermission("commandpanels.command.generate")) {
            ctx.text.sendHelp(sender, Message.HELP_GENERATE_COMMAND, Message.HELP_GENERATE_DESCRIPTION);
        }

        if (sender.hasPermission("commandpanels.command.data")) {
            ctx.text.sendHelp(sender, Message.HELP_DATA_COMMAND, Message.HELP_DATA_DESCRIPTION);
        }

        if (sender.hasPermission("commandpanels.command.version")) {
            ctx.text.sendHelp(sender, Message.HELP_VERSION_COMMAND, Message.HELP_VERSION_DESCRIPTION);
        }

        if (sender.hasPermission("commandpanels.command.help")) {
            ctx.text.sendHelp(sender, Message.HELP_HELP_COMMAND, Message.HELP_HELP_DESCRIPTION);
        }

        return true;
    }
}
