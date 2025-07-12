package me.rockyhawk.commandpanels.commands.subcommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.commands.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class VersionCommand implements SubCommand {

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public String getPermission() {
        return "commandpanels.command.version";
    }

    @Override
    public boolean execute(Context ctx, CommandSender sender, String[] args) {
        ctx.text.sendInfo(sender, "");
        sender.sendMessage(Component.text("Version ", NamedTextColor.DARK_GREEN)
                    .append(Component.text(ctx.plugin.getDescription().getVersion(), NamedTextColor.WHITE)));
        return true;
    }
}
