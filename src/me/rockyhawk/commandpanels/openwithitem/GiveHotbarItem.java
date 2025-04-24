package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class GiveHotbarItem {
    Context ctx;
    public GiveHotbarItem(Context pl) {
        this.ctx = pl;
    }

    //this will give a hotbar item to a player
    public void giveHotbarItem(CommandSender sender, Player p, Panel panel){
        if (sender.hasPermission("commandpanel.item." + panel.getConfig().getString("perm")) && panel.getConfig().contains("open-with-item")) {
            //check for disabled worlds
            if(!ctx.openPanel.permission.isPanelWorldEnabled(p,panel.getConfig())){
                sender.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
                return;
            }

            boolean sendGiveMessage = !(sender instanceof Player && sender == p);

            //if the sender has OTHER perms, or if sendGiveMessage is false, implying it is not for another person
            if(sender.hasPermission("commandpanel.other") || !sendGiveMessage) {
                try {
                    if(panel.getConfig().contains("open-with-item.stationary")) {
                        p.getInventory().setItem(Integer.parseInt(Objects.requireNonNull(panel.getConfig().getString("open-with-item.stationary"))), panel.getHotbarItem(p));
                    }else{
                        p.getInventory().addItem(panel.getHotbarItem(p));
                    }
                    if(sendGiveMessage) {
                        sender.sendMessage(ctx.text.colour( ctx.tag + ChatColor.GREEN + "Item Given to " + p.getDisplayName()));
                    }
                } catch (Exception r) {
                    sender.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.notitem")));
                }
            }else{
                sender.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
            }
            return;
        }
        if (!panel.getConfig().contains("open-with-item")) {
            sender.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.noitem")));
            return;
        }
        sender.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
    }
}
