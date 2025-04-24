package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;


public class ListCommand implements CommandExecutor {
    Context ctx;
    public ListCommand(Context pl) { this.ctx = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("cpl") || label.equalsIgnoreCase("commandpanellist") || label.equalsIgnoreCase("cpanell")) {
            if (sender.hasPermission("commandpanel.list")) {
                //command /cpl
                //check to make sure the panels isn't empty
                try {
                    if (ctx.plugin.panelList == null) {
                        sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "No panels found!"));
                        return true;
                    }
                }catch(Exception b){
                    sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "No panels found!"));
                    return true;
                }

                ArrayList<Panel> panels = new ArrayList<>(ctx.plugin.panelList);
                int page = 1;
                int skip = 0;
                if(args.length == 1){
                    try {
                        page = Integer.parseInt(args[0]);
                        skip = page*8-8;
                    }catch (Exception e){
                        sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Inaccessible Page"));
                    }
                }
                sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.DARK_AQUA + "Panels: (Page " + page + ")"));
                for (int f = skip; panels.size() > f && skip+8 > f; f++) {
                    if(panels.get(f).getFile() == null){ continue; }

                    sender.sendMessage(ChatColor.DARK_GREEN + panels.get(f).getFile().getAbsolutePath().replace(ctx.configHandler.panelsFolder.getAbsolutePath(),"") + ChatColor.GREEN + " " + panels.get(f).getName());
                    if(panels.size()-1 == f){
                        return true;
                    }
                }
                sender.sendMessage(ChatColor.AQUA + "Type /cpl " + (page+1) + " to read next page");
            }else{
                sender.sendMessage(ctx.tex.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
            }
            return true;
        }
        sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Usage: /cpl"));
        return true;
    }
}