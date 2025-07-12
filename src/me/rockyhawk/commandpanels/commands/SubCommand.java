package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.command.CommandSender;

public interface SubCommand {
    String getName();
    String getPermission();
    boolean execute(Context ctx, CommandSender sender, String[] args);

}