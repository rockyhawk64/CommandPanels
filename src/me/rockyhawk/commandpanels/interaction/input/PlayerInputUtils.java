package me.rockyhawk.commandpanels.interaction.input;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PlayerInputUtils implements Listener {
    Context ctx;
    public PlayerInputUtils(Context pl) {
        this.ctx = pl;
    }

    public HashMap<Player, PlayerInput> playerInput = new HashMap<>();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if(playerInput.containsKey(e.getPlayer())){
            e.setCancelled(true);
            if(e.getMessage().equalsIgnoreCase(ctx.configHandler.config.getString("input.input-cancel"))){
                if(playerInput.get(e.getPlayer()).cancelCommands != null){
                    final PlayerInput taskInput = playerInput.remove(e.getPlayer());
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ctx.plugin, new Runnable() {
                        public void run() {
                            if(taskInput.cancelCommands != null){
                                ctx.commands.runCommands(taskInput.panel, PanelPosition.Top,e.getPlayer(), taskInput.cancelCommands,taskInput.click); //Have to do this to run regular Bukkit voids in an ASYNC Event
                            }
                        }
                    });
                } else {
                    playerInput.remove(e.getPlayer());
                }
                return;
            }
            playerInput.get(e.getPlayer()).panel.placeholders.addPlaceholder("player-input",e.getMessage());

            
            if((playerInput.get(e.getPlayer()).panel.getConfig().getString("max-input-length") != null) && (Integer.parseInt(playerInput.get(e.getPlayer()).panel.getConfig().getString("max-input-length")) != -1) && (e.getMessage().length() > Integer.parseInt(playerInput.get(e.getPlayer()).panel.getConfig().getString("max-input-length")))) {
                e.getPlayer().sendMessage(ctx.text.colour(ctx.tag + playerInput.get(e.getPlayer()).panel.getConfig().getString("custom-messages.input")));
                return;
            }else if(e.getMessage().length() > Integer.parseInt(ctx.configHandler.config.getString("input.max-input-length")) && (Integer.parseInt(ctx.configHandler.config.getString("input.max-input-length")) != -1)) {
                e.getPlayer().sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.input")));
                return;
            }
            //get certain words from the input
            int c = 0;
            for(String message : e.getMessage().split("\\s")){
                playerInput.get(e.getPlayer()).panel.placeholders.addPlaceholder("player-input-" + (c+1),message);
                c++;
            }

            final PlayerInput taskInput = playerInput.remove(e.getPlayer());
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ctx.plugin, new Runnable() {
                public void run() {
                    ctx.commands.runCommands(taskInput.panel, PanelPosition.Top,e.getPlayer(), taskInput.commands,taskInput.click); //I have to do this to run regular Bukkit voids in an ASYNC Event
                }
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        //if the player is in generate mode, remove generate mode
        playerInput.remove(e.getPlayer());
    }

    public void sendInputMessage(Panel panel, PanelPosition pos, Player p){
        List<String> inputMessages;
        if(panel.getConfig().contains("custom-messages.input-message")){
            //For input-message custom from in the panel
            inputMessages = new ArrayList<>(panel.getConfig().getStringList("custom-messages.input-message"));
        }else{
            //For input-message from the config
            inputMessages = new ArrayList<>(ctx.configHandler.config.getStringList("input.input-message"));
        }
        for (String temp : inputMessages) {
            temp = temp.replaceAll("%cp-args%", Objects.requireNonNull(ctx.configHandler.config.getString("input.input-cancel")));
            p.sendMessage(ctx.text.placeholders(panel,pos,p, temp));
        }
    }
}
