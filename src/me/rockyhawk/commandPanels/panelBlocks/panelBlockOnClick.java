package me.rockyhawk.commandPanels.panelBlocks;

import me.clip.placeholderapi.PlaceholderAPI;
import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class panelBlockOnClick implements Listener {
    commandpanels plugin;
    public panelBlockOnClick(commandpanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        String tag = plugin.config.getString("config.format.tag") + " ";
        //if panel blocks are disabled return
        if(Objects.requireNonNull(plugin.config.getString("")).equalsIgnoreCase("false")){
            return;
        }
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();
            Player p = e.getPlayer();
            assert block != null;
            if(plugin.blockConfig.contains("blocks")){
                if(Objects.requireNonNull(plugin.config.getString("config.panel-blocks")).equalsIgnoreCase("false")){
                    return;
                }
                for (String configLocation : Objects.requireNonNull(plugin.blockConfig.getConfigurationSection("blocks")).getKeys(false)) {
                    String[] loc = configLocation.split("_");
                    Location tempLocation = new Location(plugin.getServer().getWorld(loc[0]),Double.parseDouble(loc[1]),Double.parseDouble(loc[2]),Double.parseDouble(loc[3]));
                    if(tempLocation.equals(block.getLocation())){
                        e.setCancelled(true);
                        Bukkit.dispatchCommand(p, "commandpanels:commandpanel " + plugin.blockConfig.getString("blocks." + configLocation + ".panel"));
                        return;
                    }
                }
            }
        }
    }
}