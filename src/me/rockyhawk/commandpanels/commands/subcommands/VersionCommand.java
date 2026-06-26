package me.rockyhawk.commandpanels.commands.subcommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.commands.SubCommand;
import me.rockyhawk.commandpanels.formatter.language.Message;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class VersionCommand implements SubCommand {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

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
        String devTemplate = ensurePlaceholder(ctx.text.lang.translate(Message.PLUGIN_DEVELOPER));
        String verTemplate = ensurePlaceholder(ctx.text.lang.translate(Message.PLUGIN_VERSION));

        sender.sendMessage(ctx.text.getPrefix());
        sender.sendMessage(miniMessage.deserialize(
                "<dark_aqua>" + devTemplate.replace("{0}", "<white>RockyHawk")));
        sender.sendMessage(miniMessage.deserialize(
                "<dark_aqua>" + verTemplate.replace("{0}", "<white>" + ctx.plugin.getPluginMeta().getVersion())));

        return true;
    }

    private String ensurePlaceholder(String text) {
        return text.contains("{0}") ? text : text + " {0}";
    }

}