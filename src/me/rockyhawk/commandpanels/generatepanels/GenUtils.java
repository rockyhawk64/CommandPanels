package me.rockyhawk.commandpanels.generatepanels;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class GenUtils implements Listener {
    public YamlConfiguration tempEdit;
    CommandPanels plugin;
    public GenUtils(CommandPanels pl) {
        this.plugin = pl;
        this.tempEdit = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "temp.yml"));
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player)e.getPlayer();
        if(!ChatColor.stripColor(e.getView().getTitle()).equals("Generate New Panel")){
            return;
        }
        //reload panel files to avoid conflicts
        plugin.reloadPanelFiles();
        generatePanel(p,e.getInventory());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        //if the player is in generate mode, remove generate mode
        this.plugin.generateMode.remove(p);
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent e) {
        HumanEntity h = e.getPlayer();
        Player p = Bukkit.getPlayer(h.getName());
        if (this.plugin.generateMode.contains(p)) {
            this.plugin.generateMode.remove(p);
            generatePanel(p,e.getInventory());
        }
    }

    void generatePanel(Player p, Inventory inv){
        ArrayList<String> apanels = new ArrayList();
        for(Panel panel : plugin.panelList){
            //create list of names that aren't a String list
            apanels.add(panel.getName());
        }
        //this is done to make sure the inventories are not empty
        boolean foundItem = false;
        for(ItemStack temp : inv.getContents()){
            if(temp != null){
                foundItem = true;
                break;
            }
        }
        if(!foundItem){
            //panels don't need items but I cancel on generate with no items because then players have the option to cancel if they need to
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Cancelled Panel!"));
            return;
        }
        YamlConfiguration file;
        //String date: is what the panel and file name will be called
        String date = "panel-1";
        for(int count = 1; (Arrays.asList(Objects.requireNonNull(plugin.panelsf.list())).contains("panel-" + count + ".yml")) || (apanels.contains("panel-" + count)); count++){
            date = "panel-" + (count+1);
        }
        File folder = new File(plugin.getDataFolder() + File.separator + "panels");
        file = YamlConfiguration.loadConfiguration(new File(folder + File.separator + date + ".yml"));
        file.set("panels." + date + ".perm", "default");

        if(inv.getType().toString().contains("CHEST")){
            file.set("panels." + date + ".rows", inv.getSize()/9);
        }else{
            file.set("panels." + date + ".rows", inv.getType().toString());
        }

        file.set("panels." + date + ".title", "&8Generated " + date);
        file.addDefault("panels." + date + ".command", date);
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)) {
            file.set("panels." + date + ".empty", "STAINED_GLASS_PANE");
            file.set("panels." + date + ".emptyID", "15");
        }else{
            file.set("panels." + date + ".empty", "BLACK_STAINED_GLASS_PANE");
        }
        //add items
        file = plugin.itemCreate.generatePanelFile(date,inv,file);

        try {
            file.save(new File(plugin.panelsf + File.separator + date + ".yml"));
            p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.GREEN + "Saved Generated File To: " + date + ".yml"));
        } catch (IOException var16) {
            p.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Could Not Save Generated Panel!"));
        }
        plugin.reloadPanelFiles();
    }
}