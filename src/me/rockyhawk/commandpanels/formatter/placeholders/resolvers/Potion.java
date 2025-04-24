package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

public class Potion implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("potion-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String slot_key = identifier.replace("potion-", "");
        int slotIndex = (int) Double.parseDouble(slot_key);

        // Get the item in the specified slot
        ItemStack item = p.getOpenInventory().getTopInventory().getItem(slotIndex);

        // Check if the item is not null and has potion meta
        if (item != null && item.hasItemMeta() && item.getItemMeta() instanceof PotionMeta) {
            //choose between legacy PotionData (pre 1.20.5) or PotionType
            if(ctx.version.isBelow("1.20.5")){
                //Returns the value like this <Type>:<Extended>:<Upgraded> Example SLOWNESS:true:false
                return ctx.legacyPotion.retrievePotionData(item).replaceAll("\\s",":");
            }else{
                //post 1.20.5 compare just return PotionType
                PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
                return potionMeta.getBasePotionType().toString();
            }
        } else {
            return "empty"; // Item is either null or doesn't have potion meta
        }
    }
}