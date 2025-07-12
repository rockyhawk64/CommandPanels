package me.rockyhawk.commandpanels.commands.subcommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GenerateCommand implements SubCommand {

    @Override
    public String getName() {
        return "generate";
    }

    @Override
    public String getPermission() {
        return "commandpanels.command.generate";
    }

    @Override
    public boolean execute(Context ctx, CommandSender sender, String[] args) {
        if(sender instanceof Player p) {
            ctx.generator.startGenerateMode(p);
        }else{
            ctx.text.sendError(sender, "You must be a player.");
        }


        return true;
    }
}
