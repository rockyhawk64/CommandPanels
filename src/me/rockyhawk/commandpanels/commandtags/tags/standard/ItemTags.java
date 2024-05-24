package me.rockyhawk.commandpanels.commandtags.tags.standard;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.CommandTagEvent;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Set;

public class ItemTags implements Listener {
    CommandPanels plugin;
    public ItemTags(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("give-item=")){
            e.commandTagUsed();
            ItemStack itm = plugin.itemCreate.makeCustomItemFromConfig(null,e.pos,e.panel.getConfig().getConfigurationSection("custom-item." + e.args[0]), e.p, true, true, false);
            if(e.args.length == 2){
                try{
                    itm.setAmount(Integer.parseInt(e.args[1]));
                } catch (Exception err){
                    plugin.debug(err,e.p);
                }
            }
            plugin.inventorySaver.addItem(e.p,itm);
            return;
        }
        if(e.name.equalsIgnoreCase("setitem=")){
            e.commandTagUsed();
            //if player uses setitem= [custom item] [slot] [position] it will change the item slot to something, used for placeable items
            //make a section in the panel called "custom-item" then whatever the title of the item is, put that here
            ItemStack s = plugin.itemCreate.makeItemFromConfig(null, e.pos,e.panel.getConfig().getConfigurationSection("custom-item." + e.args[0]), e.p, true, true, false);
            PanelPosition position = PanelPosition.valueOf(e.args[2]);
            if(position == PanelPosition.Top) {
                e.p.getOpenInventory().getTopInventory().setItem(Integer.parseInt(e.args[1]), s);
            }else if(position == PanelPosition.Middle) {
                e.p.getInventory().setItem(Integer.parseInt(e.args[1])+9, s);
            }else{
                e.p.getInventory().setItem(Integer.parseInt(e.args[1]), s);
            }
            return;
        }
        if(e.name.equalsIgnoreCase("enchant=")){
            e.commandTagUsed();
            //if player uses enchant= [slot] [position] [ADD/REMOVE/CLEAR] [enchant] <level> it will add/remove/clear the enchants of the selected slot
            PanelPosition position = PanelPosition.valueOf(e.args[1]);
            ItemStack EditItem;
            if(position == PanelPosition.Top) {
                EditItem = e.p.getOpenInventory().getTopInventory().getItem(Integer.parseInt(e.args[0]));
            }else if(position == PanelPosition.Middle) {
                EditItem = e.p.getInventory().getItem(Integer.parseInt(e.args[0])+9);
            }else{
                EditItem = e.p.getInventory().getItem(Integer.parseInt(e.args[0]));
            }

            assert EditItem != null;

            if(e.args[2].equalsIgnoreCase("add")){
                try{
                    Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(e.args[3].toLowerCase()));
                    assert enchant != null;
                    EditItem.addEnchantment(enchant, Integer.parseInt(e.args[4]));
                    return;
                } catch (Exception err){
                    plugin.debug(err,e.p);
                }
            }

            if(e.args[2].equalsIgnoreCase("remove")){
                try{
                    Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(e.args[3].toLowerCase()));
                    assert enchant != null;
                    EditItem.removeEnchantment(enchant);
                    return;
                } catch (Exception err){
                    plugin.debug(err,e.p);
                }
            }

            if(e.args[2].equalsIgnoreCase("clear")){
                try{
                    Set<Enchantment> Enchants = EditItem.getEnchantments().keySet();
                    for(Enchantment enchant : Enchants){
                        EditItem.removeEnchantment(enchant);
                    }
                } catch (Exception err){
                    plugin.debug(err,e.p);
                }
            }

            return;
        }
        if(e.name.equalsIgnoreCase("setcustomdata=")){
            e.commandTagUsed();
            //if player uses setcustomdata= [slot] [position] [data] it will change the custom model data of the item
            PanelPosition position = PanelPosition.valueOf(e.args[1]);
            ItemStack EditItem;
            if(position == PanelPosition.Top) {
                EditItem = e.p.getOpenInventory().getTopInventory().getItem(Integer.parseInt(e.args[0]));
            }else if(position == PanelPosition.Middle) {
                EditItem = e.p.getInventory().getItem(Integer.parseInt(e.args[0])+9);
            }else{
                EditItem = e.p.getInventory().getItem(Integer.parseInt(e.args[0]));
            }

            assert EditItem != null;

            try{
                if(EditItem.hasItemMeta()){
                    Objects.requireNonNull(EditItem.getItemMeta()).setCustomModelData(Integer.valueOf(e.args[2]));
                }
            } catch (Exception err){
                plugin.debug(err,e.p);
            }

            return;
        }
    }
}
