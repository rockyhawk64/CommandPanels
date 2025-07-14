package me.rockyhawk.commandpanels.commands.subcommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.Converter;
import me.rockyhawk.commandpanels.commands.SubCommand;
import org.bukkit.command.CommandSender;

public class ConvertCommand implements SubCommand {

    @Override
    public String getName() {
        return "convert";
    }

    @Override
    public String getPermission() {
        return "commandpanels.command.convert";
    }

    @Override
    public boolean execute(Context ctx, CommandSender sender, String[] args) {
        Converter converter = new Converter(ctx);
        converter.convertPanels(sender);
        ctx.fileHandler.reloadPanels();
        return true;
    }
}
