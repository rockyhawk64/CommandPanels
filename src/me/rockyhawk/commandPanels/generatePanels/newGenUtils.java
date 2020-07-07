package me.rockyhawk.commandPanels.generatePanels;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class newGenUtils implements Listener {
    public YamlConfiguration tempEdit;
    commandpanels plugin;
    public newGenUtils(commandpanels pl) {
        this.plugin = pl;
        this.tempEdit = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "temp.yml"));
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        String tag = plugin.config.getString("config.format.tag") + " ";
        Player p = (Player)e.getPlayer();
        if (!ChatColor.stripColor(e.getView().getTitle()).equals("Generate New Panel")){
            return;
        }
        //reload panel files to avoid conflicts
        plugin.reloadPanelFiles();
        //get all panel names (not titles)
        File panelsf = new File(plugin.getDataFolder() + File.separator + "panels");
        Boolean pexist = true;
        //pexist is true if panels exist
        YamlConfiguration cf;
        ArrayList<String> apanels = new ArrayList<String>(); //all panels from all files (panel names)
        try {
            for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
                YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
                if(!plugin.checkPanels(temp)){
                    continue;
                }
                for (String key : temp.getConfigurationSection("panels").getKeys(false)) {
                    apanels.add(key);
                }
            }
        }catch(Exception fail){
            //could not fetch all panel names (probably no panels exist)
            pexist = false;
        }
        ItemStack[] cont = e.getInventory().getContents();
        boolean foundItem = false;
        for(ItemStack temp : cont){
            if(temp != null){
                foundItem = true;
                break;
            }
        }
        if(!foundItem){
            //panels don't need items but I cancel on generate with no items because then players have the option to cancel if they need to
            p.sendMessage(plugin.papi(p,tag + ChatColor.RED + "Cancelled Panel!"));
            return;
        }
        YamlConfiguration file;
        //String date: is what the panel and file name will be called
        String date = "panel-1";
        if(pexist){
            for(int count = 1; (Arrays.asList(panelsf.list()).contains("panel-" + count + ".yml")) || (apanels.contains("panel-" + count)); count++){
                date = "panel-" + (count+1);
            }
        }else{
            date = "panel-1";
        }
        //String date = new SimpleDateFormat("dd-HH-mm-ss").format(new Date());
        File folder = new File(plugin.getDataFolder() + File.separator + "panels");
        file = YamlConfiguration.loadConfiguration(new File(folder + File.separator + date + ".yml"));
        file.addDefault("panels." + date + ".perm", "default");
        file.addDefault("panels." + date + ".rows", e.getInventory().getSize()/9);
        file.addDefault("panels." + date + ".title", "&8Generated " + date);
        file.addDefault("panels." + date + ".command", date);
        file.addDefault("panels." + date + ".empty", "BLACK_STAINED_GLASS_PANE");
        for(int i = 0; cont.length > i; i++){
            //repeat through all the items in the chest
            try{
                //make the item here
                if(cont[i].getType() == Material.PLAYER_HEAD){
                    if(plugin.getHeadBase64(cont[i]) != null){
                        file.addDefault("panels." + date + ".item." + i + ".material", "cps= " + plugin.getHeadBase64(cont[i]));
                    }else{
                        file.addDefault("panels." + date + ".item." + i + ".material", cont[i].getType().toString());
                    }
                }else {
                    file.addDefault("panels." + date + ".item." + i + ".material", cont[i].getType().toString());
                }
                if(cont[i].getAmount() != 1){
                    file.addDefault("panels." + date + ".item." + i + ".stack", cont[i].getAmount());
                }
                if(!cont[i].getEnchantments().isEmpty()){
                    file.addDefault("panels." + date + ".item." + i + ".enchanted", "true");
                }
                try {
                    PotionMeta potionMeta = (PotionMeta) cont[i].getItemMeta();
                    file.addDefault("panels." + date + ".item." + i + ".potion", potionMeta.getBasePotionData().getType().name());
                } catch (Exception er) {
                    //don't add the effect
                }
                file.addDefault("panels." + date + ".item." + i + ".name", cont[i].getItemMeta().getDisplayName());
                file.addDefault("panels." + date + ".item." + i + ".lore", cont[i].getItemMeta().getLore());
            }catch(Exception n){
                //skip over an item that spits an error
            }
        }
        file.options().copyDefaults(true);
        try {
            file.save(new File(folder + File.separator + date + ".yml"));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.GREEN + "Saved Generated File To: " + date + ".yml"));
        } catch (IOException s) {
            plugin.debug(s);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "Could Not Save Generated Panel!"));
        }
        plugin.reloadPanelFiles();
    }
}