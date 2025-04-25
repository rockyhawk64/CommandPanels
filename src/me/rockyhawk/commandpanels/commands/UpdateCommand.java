package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;


public class UpdateCommand implements CommandExecutor {
    Context ctx;
    public UpdateCommand(Context pl) { this.ctx = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("cpu") || label.equalsIgnoreCase("commandpanelupdate") || label.equalsIgnoreCase("cpanelu")) {
            if (sender.hasPermission("commandpanel.refresh")) {
                //command /cpu (uses .refresh permission node)
                // /cpu <Playername> <Position/ALL>

                String name;
                Player targetPlayer;
                try {
                    name = args[0];
                    targetPlayer = Bukkit.getPlayer(name);
                }catch (Exception e){
                    sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.RED + "Player was not found."));
                    return true;
                }
                assert targetPlayer != null;

                PanelPosition pp;
                if(args[1].equalsIgnoreCase("all")){
                    for(PanelPosition papo : PanelPosition.values()){
                        if(ctx.openPanels.hasPanelOpen(name, papo)) {
                            new PanelBuilder(ctx).refreshInv(ctx.openPanels.getOpenPanel(name, papo), targetPlayer, papo, 0);
                        }
                    }
                } else {
                    try {
                        pp = PanelPosition.valueOf(args[1]);
                    }catch (Exception e){
                        sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.RED + "Panel position not found."));
                        return true;
                    }

                    if(ctx.openPanels.hasPanelOpen(name, pp)) {
                        new PanelBuilder(ctx).refreshInv(ctx.openPanels.getOpenPanel(name, pp), targetPlayer, pp, 0);
                    }
                }


                if(ctx.inventorySaver.hasNormalInventory(targetPlayer)){
                    ctx.hotbar.updateHotbarItems(targetPlayer);
                }

                //Successfully refreshed panel for targetPlayer.getName()
                sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.GREEN + "Successfully refreshed panel for " + targetPlayer.getName() + "."));
            }else{
                sender.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
            }
            return true;
        }
        sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.RED + "Usage: /cpu <Playername> <Position/ALL>"));
        return true;
    }
}