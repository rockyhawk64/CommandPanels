package me.rockyhawk.commandpanels.generate;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
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
import java.util.List;
import java.util.Objects;

public class GenUtils implements Listener {
    public YamlConfiguration tempEdit;
    public List<Player> generateMode = new ArrayList<>(); //players that are currently in generate mode

    Context ctx;
    public GenUtils(Context pl) {
        this.ctx = pl;
        this.tempEdit = YamlConfiguration.loadConfiguration(new File(ctx.plugin.getDataFolder() + File.separator + "temp.yml"));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player)e.getPlayer();
        if(!p.hasPermission("commandpanel.generate")){
            return;
        }
        if(!ChatColor.stripColor(e.getView().getTitle()).equals("Generate New Panel")){
            return;
        }
        //reload panel files to avoid conflicts
        ctx.reloader.reloadPanelFiles();
        generatePanel(p,e.getInventory());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        //if the player is in generate mode, remove generate mode
        generateMode.remove(p);
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent e) {
        HumanEntity h = e.getPlayer();
        Player p = Bukkit.getPlayer(h.getName());
        if (generateMode.contains(p)) {
            generateMode.remove(p);
            generatePanel(p,e.getInventory());
        }
    }

    private void generatePanel(Player p, Inventory inv){
        ArrayList<String> apanels = new ArrayList();
        for(Panel panel : ctx.plugin.panelList){
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
            p.sendMessage(ctx.text.colour(ctx.tag + ChatColor.RED + "Cancelled Panel!"));
            return;
        }
        YamlConfiguration file;
        //String date: is what the panel and file name will be called
        String date = "panel-1";
        for(int count = 1; (Arrays.asList(Objects.requireNonNull(ctx.configHandler.panelsFolder.list())).contains("panel-" + count + ".yml")) || (apanels.contains("panel-" + count)); count++){
            date = "panel-" + (count+1);
        }
        File folder = new File(ctx.plugin.getDataFolder() + File.separator + "panels");
        file = YamlConfiguration.loadConfiguration(new File(folder + File.separator + date + ".yml"));
        file.set("panels." + date + ".perm", "default");

        if(inv.getType().toString().contains("CHEST")){
            file.set("panels." + date + ".rows", inv.getSize()/9);
        }else{
            file.set("panels." + date + ".rows", inv.getType().toString());
        }

        file.set("panels." + date + ".title", "&8Generated " + date);
        file.addDefault("panels." + date + ".command", date);
        if(ctx.version.isBelow("1.13")){
            file.set("panels." + date + ".empty", "STAINED_GLASS_PANE");
            file.set("panels." + date + ".emptyID", "15");
        }else{
            file.set("panels." + date + ".empty", "BLACK_STAINED_GLASS_PANE");
        }
        //add items
        file = ctx.itemCreate.generatePanelFile(date,inv,file);

        try {
            file.save(new File(ctx.configHandler.panelsFolder + File.separator + date + ".yml"));
            p.sendMessage(ctx.text.colour( ctx.tag + ChatColor.GREEN + "Saved Generated File To: " + date + ".yml"));
        } catch (IOException var16) {
            p.sendMessage(ctx.text.colour( ctx.tag + ChatColor.RED + "Could Not Save Generated Panel!"));
        }
        ctx.reloader.reloadPanelFiles();
    }
}
