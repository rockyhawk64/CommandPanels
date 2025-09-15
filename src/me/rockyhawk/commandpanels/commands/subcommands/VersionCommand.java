package me.rockyhawk.commandpanels.commands.subcommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.commands.SubCommand;
import me.rockyhawk.commandpanels.formatter.language.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
        sender.sendMessage(ctx.text.getPrefix());

        String translatedDeveloper = ctx.text.lang.translate(Message.PLUGIN_DEVELOPER);
        String translatedVersion = ctx.text.lang.translate(Message.PLUGIN_VERSION);
        sender.sendMessage(Component.text()
                .color(NamedTextColor.DARK_AQUA)
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(translatedDeveloper))
                .build()
                .replaceText(builder -> builder
                        .match("\\{0\\}")
                        .replacement(Component.text("RockyHawk", NamedTextColor.WHITE))));
        sender.sendMessage(Component.text()
                .color(NamedTextColor.DARK_AQUA)
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(translatedVersion))
                .build()
                .replaceText(builder -> builder
                        .match("\\{0\\}")
                        .replacement(Component.text(ctx.plugin.getPluginMeta().getVersion(), NamedTextColor.WHITE))));
        return true;
    }
}
