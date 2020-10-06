package me.rockyhawk.commandpanels.panelblocks;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.util.Objects;

public class PanelBlockOnClick implements Listener {
    CommandPanels plugin;
    public PanelBlockOnClick(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        //if panel blocks are disabled return
        if(Objects.requireNonNull(plugin.config.getString("config.panel-blocks")).equalsIgnoreCase("false")){
            return;
        }
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = e.getClickedBlock();
        Player p = e.getPlayer();
        assert block != null;
        if(!plugin.blockConfig.contains("blocks")){
            return;
        }
        if(Objects.requireNonNull(plugin.config.getString("config.panel-blocks")).equalsIgnoreCase("false")){
            return;
        }
        for (String configLocation : Objects.requireNonNull(plugin.blockConfig.getConfigurationSection("blocks")).getKeys(false)) {
            String[] loc = configLocation.split("_");
            Location tempLocation = new Location(plugin.getServer().getWorld(loc[0].replaceAll("%dash%","_")),Double.parseDouble(loc[1]),Double.parseDouble(loc[2]),Double.parseDouble(loc[3]));
            if(tempLocation.equals(block.getLocation())){
                e.setCancelled(true);
                for(String[] temp : plugin.panelNames){
                    if(temp[0].equals(plugin.blockConfig.getString("blocks." + configLocation + ".panel"))){
                        String panelName = temp[0];
                        YamlConfiguration cf = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(temp[1]))));
                        plugin.openVoids.openCommandPanel(p,p,panelName,cf.getConfigurationSection("panels." + panelName),false);
                        return;
                    }
                }
            }
        }
    }
}