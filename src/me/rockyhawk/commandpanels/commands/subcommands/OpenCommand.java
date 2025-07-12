package me.rockyhawk.commandpanels.commands.subcommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import me.rockyhawk.commandpanels.builder.inventory.InventoryPanelBuilder;
import me.rockyhawk.commandpanels.commands.SubCommand;
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
            ctx.text.sendError(sender, "Usage: /pa open <panelName> [playerName]");
            return true;
        }

        if (ctx.plugin.panels.get(args[0]) == null) {
            ctx.text.sendError(sender, "Panel not found.");
            return true;
        }

        Panel panel = ctx.plugin.panels.get(args[0]);
        Player target;

        // If args length is 1 or the sender does not have permission to open
        // The panel for another user, only open for themselves
        if (args.length == 1 || !sender.hasPermission("commandpanels.command.open.other")) {
            if (!(sender instanceof Player)) {
                ctx.text.sendError(sender, "You must be a player to open a panel for yourself.");
                return true;
            }
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                ctx.text.sendError(sender, "Player is not online.");
                return true;
            }
        }

        if(sender instanceof Player && !panel.canOpen((Player) sender, ctx)){
            ctx.text.sendError(sender, "No permission.");
            return true;
        }

        if(sender != target){
            ctx.text.sendInfo(sender, "Panel open triggered for player.");
        }

        panel.open(ctx, target, SessionManager.PanelOpenType.EXTERNAL);
        return true;
    }
}
