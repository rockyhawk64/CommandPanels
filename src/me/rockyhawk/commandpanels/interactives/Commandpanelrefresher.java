package me.rockyhawk.commandpanels.interactives;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.api.PanelOpenedEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class Commandpanelrefresher implements Listener {
    CommandPanels plugin;
    public Commandpanelrefresher(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onPanelOpen(PanelOpenedEvent e){ //Handles when Players open inventory
        //I have to convert HumanEntity to a player
        if (plugin.config.contains("config.refresh-panels")) {
            if (Objects.requireNonNull(plugin.config.getString("config.refresh-panels")).trim().equalsIgnoreCase("false")) {
                return;
            }
        }

        Player p = e.getPlayer();
        Panel pn = e.getPanel();

        //remove sound-on-open on 1.8 for those who do not read the wiki ;)
        if(pn.getConfig().contains("sound-on-open")){
            if(Bukkit.getVersion().contains("1.8")){
                pn.getConfig().set("sound-on-open", null);
            }
        }

        //if panel has custom refresh delay
        int tempRefreshDelay = plugin.config.getInt("config.refresh-delay");
        if(pn.getConfig().contains("refresh-delay")){
            tempRefreshDelay = pn.getConfig().getInt("refresh-delay");
        }
        final int refreshDelay = tempRefreshDelay;

        if(pn.getConfig().contains("panelType")) {
            if (pn.getConfig().getStringList("panelType").contains("static")) {
                //do not update temporary panels, only default panels
                return;
            }
        }

        new BukkitRunnable(){
            int c = 0;
            int animatecount = 0;
            @Override
            public void run() {
                int animatevalue = -1;
                if(pn.getConfig().contains("animatevalue")){
                    animatevalue = pn.getConfig().getInt("animatevalue");
                }
                //counter counts to refresh delay (in seconds) then restarts
                if(c < refreshDelay){
                    c+=1;
                }else{
                    c=0;
                }
                //refresh here
                if(plugin.openPanels.hasPanelOpen(p.getName(),pn.getName())){
                    if(c == 0) {
                        //animation counter
                        if(animatevalue != -1) {
                            if (animatecount < animatevalue) {
                                animatecount += 1;
                            } else {
                                animatecount = 0;
                            }
                        }
                        try {
                            plugin.createGUI.openGui(pn, p, 0,animatecount);
                        } catch (Exception e) {
                            //error opening gui
                            p.closeInventory();
                            plugin.openPanels.closePanelForLoader(p.getName());
                            this.cancel();
                        }
                    }
                }else{
                    if(Objects.requireNonNull(plugin.config.getString("config.stop-sound")).trim().equalsIgnoreCase("true")){
                        try {
                            p.stopSound(Sound.valueOf(Objects.requireNonNull(pn.getConfig().getString("sound-on-open")).toUpperCase()));
                        }catch(Exception sou){
                            //skip
                        }
                    }
                    c = 0;
                    this.cancel();
                }
            }
        }.runTaskTimer(this.plugin, 1,1); //20 ticks == 1 second (5 ticks = 0.25 of a second)

    }
}
