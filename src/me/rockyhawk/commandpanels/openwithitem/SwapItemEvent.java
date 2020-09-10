package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Iterator;
import java.util.Objects;

public class SwapItemEvent implements Listener {
    CommandPanels plugin;
    public SwapItemEvent(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onPlayerSwapHandItemsEvent​(PlayerSwapHandItemsEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        Player p = e.getPlayer();
        try {
            if (plugin.panelFiles == null) {
                return;
            }
        }catch(Exception b){
            return;
        }
        String tpanels; //tpanels is the temp to check through the files
        ItemStack clicked = e.getOffHandItem();
        for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
            YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
            String key;
            tpanels = "";
            if(!plugin.checkPanels(temp)){
                continue;
            }
            for (Iterator var10 = Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false).iterator(); var10.hasNext(); tpanels = tpanels + key + " ") {
                key = (String) var10.next();
                if(temp.contains("panels." + key + ".open-with-item")){
                    assert clicked != null;
                    if(clicked.getType() != Material.AIR) {
                        //if loop has material first to stop 1.12.2 from spitting errors
                        //try and catch loop to stop errors with the same material type but different name
                        try {
                            if (clicked.getType() == new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.material")))), 1).getType()) {
                                if ((plugin.papi( Objects.requireNonNull(clicked.getItemMeta()).getDisplayName()).equals(plugin.papi( Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.name")))))) {
                                    //cancel the click item event
                                    if(temp.contains("panels." + key + ".open-with-item.stationary")){
                                        if(p.getInventory().getHeldItemSlot() == Integer.parseInt(Objects.requireNonNull(temp.getString("panels." + key + ".open-with-item.stationary")))){
                                            e.setCancelled(true);
                                        }
                                    }
                                    return;
                                }
                            }
                        }catch(NullPointerException | IllegalArgumentException n){
                            plugin.debug(n);
                        }
                    }
                }
            }
        }
    }
}
