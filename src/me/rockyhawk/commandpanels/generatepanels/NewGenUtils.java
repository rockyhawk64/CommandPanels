package me.rockyhawk.commandpanels.generatepanels;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class NewGenUtils implements Listener {
    public YamlConfiguration tempEdit;
    CommandPanels plugin;
    public NewGenUtils(CommandPanels pl) {
        this.plugin = pl;
        this.tempEdit = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "temp.yml"));
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player)e.getPlayer();
        if (!ChatColor.stripColor(e.getView().getTitle()).equals("Generate New Panel")){
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
        for(int o = 0; this.plugin.userInputStrings.size() > o; ++o) {
            if (this.plugin.userInputStrings.get(o)[0].equals(e.getPlayer().getName())) {
                this.plugin.userInputStrings.remove(o);
                break;
            }
        }
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent e) {
        HumanEntity h = e.getPlayer();
        Player p = Bukkit.getPlayer(h.getName());
        if ((e.getInventory().getHolder() instanceof Chest || e.getInventory().getHolder() instanceof DoubleChest) && this.plugin.generateMode.contains(p)) {
            this.plugin.generateMode.remove(p);
            generatePanel(p,e.getInventory());
        }
    }

    @SuppressWarnings("deprecation")
    void generatePanel(Player p, Inventory inv){
        ItemStack[] cont = inv.getContents();
        String tag = plugin.config.getString("config.format.tag") + " ";
        ArrayList<String> apanels = new ArrayList();
        for(String[] panelNames : plugin.panelNames){
            //create list of names that aren't a String list
            apanels.add(panelNames[0]);
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
            p.sendMessage(plugin.papi(tag + ChatColor.RED + "Cancelled Panel!"));
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
        file.addDefault("panels." + date + ".perm", "default");
        file.addDefault("panels." + date + ".panelType", "default");
        file.addDefault("panels." + date + ".rows", inv.getSize()/9);
        file.addDefault("panels." + date + ".title", "&8Generated " + date);
        file.addDefault("panels." + date + ".command", date);
        file.addDefault("panels." + date + ".empty", "BLACK_STAINED_GLASS_PANE");
        for(int i = 0; cont.length > i; i++){
            //repeat through all the items in the chest
            try{
                //make the item here
                if(cont[i].getType() == Material.PLAYER_HEAD){
                    SkullMeta meta = (SkullMeta) cont[i].getItemMeta();
                    if(plugin.getHeadBase64(cont[i]) != null){
                        //check for base64
                        file.addDefault("panels." + date + ".item." + i + ".material", "cps= " + plugin.getHeadBase64(cont[i]));
                    }else if(meta.hasOwner()){
                        //check for skull owner
                        file.addDefault("panels." + date + ".item." + i + ".material", "cps= " + meta.getOwner());
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
                file.addDefault("panels." + date + ".item." + i + ".name", Objects.requireNonNull(cont[i].getItemMeta()).getDisplayName());
                file.addDefault("panels." + date + ".item." + i + ".lore", Objects.requireNonNull(cont[i].getItemMeta()).getLore());
            }catch(Exception n){
                //skip over an item that spits an error
            }
        }
        file.options().copyDefaults(true);

        try {
            file.save(new File(plugin.panelsf + File.separator + date + ".yml"));
            p.sendMessage(plugin.papi( tag + ChatColor.GREEN + "Saved Generated File To: " + date + ".yml"));
        } catch (IOException var16) {
            p.sendMessage(plugin.papi( tag + ChatColor.RED + "Could Not Save Generated Panel!"));
        }
        plugin.reloadPanelFiles();
    }
}