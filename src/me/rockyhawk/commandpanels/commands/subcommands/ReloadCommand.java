package me.rockyhawk.commandpanels.commands.subcommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.commands.SubCommand;
import me.rockyhawk.commandpanels.formatter.language.Message;
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
        Bukkit.getAsyncScheduler().runNow(ctx.plugin, task -> {
            ctx.text.lang.reloadTranslations();
            ctx.fileHandler.updateConfigFiles();
            ctx.fileHandler.reloadPanels();
            ctx.panelCommand.populateCommands();
            Bukkit.getGlobalRegionScheduler().run(ctx.plugin, t ->
                    ctx.text.sendInfo(sender, Message.PLUGIN_RELOADED));
        });
        return true;
    }
}
