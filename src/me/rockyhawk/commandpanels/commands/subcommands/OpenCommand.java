package me.rockyhawk.commandpanels.commands.subcommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.commands.SubCommand;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenCommand implements SubCommand {

    @Override
    public String getName() {
        return "open";
    }

    @Override
    public String getPermission() {
        return "commandpanels.command.open";
    }

    @Override
    public boolean execute(Context ctx, CommandSender sender, String[] args) {
        if (args.length < 1 || args.length > 2) {
            ctx.text.sendError(sender, Message.PANEL_OPEN_USAGE);
            return true;
        }

        if (ctx.plugin.panels.get(args[0]) == null) {
            ctx.text.sendError(sender, Message.PANEL_NOT_FOUND);
            return true;
        }

        Panel panel = ctx.plugin.panels.get(args[0]);
        Player target;

        // If args length is 1 or the sender does not have permission to open
        // The panel for another user, only open for themselves
        if (args.length == 1 || !sender.hasPermission("commandpanels.command.open.other")) {
            if (!(sender instanceof Player)) {
                ctx.text.sendError(sender, Message.PANEL_OPEN_PLAYER_REQUIRED);
                return true;
            }
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                ctx.text.sendError(sender, Message.PANEL_OPEN_PLAYER_OFFLINE);
                return true;
            }
        }

        if(sender instanceof Player && !panel.canOpen((Player) sender, ctx)){
            ctx.text.sendError(sender, Message.COMMAND_NO_PERMISSION);
            return true;
        }

        if(sender != target){
            ctx.text.sendInfo(sender, Message.PANEL_OPEN_TRIGGERED);
        }

        panel.open(ctx, target, SessionManager.PanelOpenType.EXTERNAL);
        return true;
    }
}
