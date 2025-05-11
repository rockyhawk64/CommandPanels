package me.rockyhawk.commandpanels.items.dropitem;
import com.loohp.platformscheduler.ScheduledRunnable;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.events.PanelClosedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class DroppedItemHandler implements Listener {
    Context ctx;
    public DroppedItemHandler(Context pl) {
        this.ctx = pl;
    }

    @EventHandler
    public void panelCloseItemsDrop(PanelClosedEvent e){
        new ScheduledRunnable(){
            @Override
            public void run(){
                for(String item : e.getPanel().getConfig().getConfigurationSection("item").getKeys(false)){
                    if(e.getPanel().getConfig().isSet("item." + item + ".itemType")){
                        //either the panel will drop the item or it will return to the inventory, no option to do both obviously
                        if(e.getPanel().getConfig().getStringList("item." + item + ".itemType").contains("dropItem")){
                            ItemStack stack = e.getPlayer().getOpenInventory().getTopInventory().getItem(Integer.parseInt(item));
                            if(stack == null || stack.getType() == Material.AIR){
                                continue;
                            }

                            //trigger event and check for cancel
                            PanelItemDropEvent dropEvent = new PanelItemDropEvent(e.getPlayer(),e.getPanel(),stack);
                            Bukkit.getPluginManager().callEvent(dropEvent);
                            if(dropEvent.isCancelled()){
                                continue;
                            }

                            e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(),stack);
                        }else if(e.getPanel().getConfig().getStringList("item." + item + ".itemType").contains("returnItem")){
                            //Remove item is listed just because website is not updated yet.
                            ItemStack stack = e.getPlayer().getOpenInventory().getTopInventory().getItem(Integer.parseInt(item));
                            if(stack == null || stack.getType() == Material.AIR){
                                continue;
                            }
                            ctx.inventorySaver.addItem(e.getPlayer(),stack);
                        }
                    }
                }
            }
        }.run();
    }
}
