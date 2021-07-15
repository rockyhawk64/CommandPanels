package me.rockyhawk.commandpanels.interactives.input;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class UserInputUtils implements Listener {
    CommandPanels plugin;
    public UserInputUtils(CommandPanels pl) {
        this.plugin = pl;
    }

    public HashMap<Player, PlayerInput> playerInput = new HashMap<>();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if(playerInput.containsKey(e.getPlayer())){
            e.setCancelled(true);
            if(e.getMessage().equalsIgnoreCase(plugin.config.getString("input.input-cancel"))){
                e.getPlayer().sendMessage(plugin.tex.colour( Objects.requireNonNull(plugin.config.getString("config.input-cancelled"))));
                playerInput.remove(e.getPlayer());
                return;
            }
            playerInput.get(e.getPlayer()).panel.placeholders.addPlaceholder("player-input",e.getMessage());

            //get certain words from the input
            int c = 0;
            for(String message : e.getMessage().split("\\s")){
                playerInput.get(e.getPlayer()).panel.placeholders.addPlaceholder("player-input-" + (c+1),message);
                c++;
            }

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    plugin.commandTags.runCommands(playerInput.get(e.getPlayer()).panel, PanelPosition.Top,e.getPlayer(), playerInput.get(e.getPlayer()).commands); //I have to do this to run regular Bukkit voids in an ASYNC Event
                    playerInput.remove(e.getPlayer());
                }
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        //if the player is in generate mode, remove generate mode
        playerInput.remove(e.getPlayer());
    }

    public void sendMessage(Panel panel, PanelPosition pos, Player p){
        List<String> inputMessages = new ArrayList<>(plugin.config.getStringList("input.input-message"));
        for (String temp : inputMessages) {
            temp = temp.replaceAll("%cp-args%", Objects.requireNonNull(plugin.config.getString("input.input-cancel")));
            p.sendMessage(plugin.tex.placeholders(panel,pos,p, temp));
        }
    }
}
