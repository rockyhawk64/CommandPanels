package me.rockyhawk.commandpanels.configuration;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class DebugManager {
    public HashSet<Player> debugSet = new HashSet<>();
    public boolean consoleDebug = false;

    public boolean isEnabled(Player p){
        return debugSet.contains(p);
    }

    public boolean isEnabled(CommandSender sender){
        if(sender instanceof Player){
            Player p = (Player)sender;
            return isEnabled(p);
        }
        return consoleDebug;
    }

    public void send(Exception e, Player p, Context context) {
        if (p == null) {
            if(consoleDebug){
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "[CommandPanels] The plugin has generated a debug error, find the error below");
                e.printStackTrace();
            }
        }else{
            if(isEnabled(p)){
                p.sendMessage(context.tag + ChatColor.DARK_RED + "Check the console for a detailed error.");
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "[CommandPanels] The plugin has generated a debug error, find the error below");
                e.printStackTrace();
            }
        }
    }
}
