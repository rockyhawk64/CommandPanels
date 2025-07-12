package me.rockyhawk.commandpanels.commands.subcommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements SubCommand {

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "commandpanels.command.reload";
    }

    @Override
    public boolean execute(Context ctx, CommandSender sender, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(ctx.plugin, () -> {
            ctx.text.lang.reloadTranslations();
            ctx.fileHandler.updateConfigFiles();
            ctx.fileHandler.reloadPanels();
            ctx.panelCommand.populateCommands();
            Bukkit.getScheduler().runTask(ctx.plugin, () -> {
                ctx.text.sendInfo(sender, "Plugin Reloaded.");
            });
        });
        return true;
    }
}
