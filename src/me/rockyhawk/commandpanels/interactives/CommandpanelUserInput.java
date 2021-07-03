package me.rockyhawk.commandpanels.interactives;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.CommandTags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Objects;

public class CommandpanelUserInput implements Listener {
    CommandPanels plugin;
    public CommandpanelUserInput(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        for(int o = 0; plugin.userInputStrings.size() > o; o++){
            if(plugin.userInputStrings.get(o)[0].equals(e.getPlayer().getName())){
                if(e.getMessage().equalsIgnoreCase(plugin.config.getString("config.input-cancel"))){
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(plugin.tex.colour( Objects.requireNonNull(plugin.config.getString("config.input-cancelled"))));
                    for(int i = 0; plugin.userInputStrings.size() > i; i++){
                        if(plugin.userInputStrings.get(i)[0].equals(e.getPlayer().getName())){
                            plugin.userInputStrings.remove(i);
                            //this is here because if one command is removed, i cannot increase by one
                            i=i-1;
                        }
                    }
                    return;
                }
                String command = plugin.userInputStrings.get(o)[1].replaceAll("%cp-player-input%", e.getMessage());
                plugin.userInputStrings.remove(o);
                o=o-1;
                e.setCancelled(true);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        new CommandTags(plugin).runCommand(null,e.getPlayer(), command); //I have to do this to run regular Bukkit voids in an ASYNC Event
                    }
                });
            }
        }
    }
}
