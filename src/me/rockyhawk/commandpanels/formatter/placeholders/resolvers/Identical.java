package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import io.lumine.mythic.lib.api.item.NBTItem;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class Identical implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("identical-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String matLocSlot = identifier.replace("identical-", "");
        String matLoc = matLocSlot.split(",")[0];
        int matSlot = (int)Double.parseDouble(matLocSlot.split(",")[1]);
        boolean isIdentical = false;
        ItemStack itm = p.getOpenInventory().getTopInventory().getItem(matSlot);

        if(itm == null){
            //continue if material is null
            return "false";
        }

        try {
            //if it is a regular custom item
            ItemStack confItm = ctx.itemCreate.makeItemFromConfig(panel,position,panel.getConfig().getConfigurationSection("custom-item." + matLoc),p,true,true, false);
            if(ctx.itemCreate.isIdentical(confItm,itm, Objects.requireNonNull(panel.getConfig().getConfigurationSection("custom-item." + matLoc)).contains("nbt"))){
                isIdentical = true;
            }

            //if custom item is an mmo item (1.14+ for the API)
            String customItemMaterial = panel.getConfig().getString("custom-item." + matLoc + ".material");
            if (Bukkit.getServer().getPluginManager().isPluginEnabled("MMOItems") && customItemMaterial.startsWith("mmo=")) {
                String mmoType = customItemMaterial.split("\\s")[1];
                String mmoID = customItemMaterial.split("\\s")[2];

                if (isMMOItem(itm,mmoType,mmoID,ctx) && itm.getAmount() <= confItm.getAmount()) {
                    isIdentical = true;
                }
            }
        } catch (NullPointerException er) {
            isIdentical = false;
        }

        return String.valueOf(isIdentical);
    }

    private boolean isMMOItem(ItemStack itm, String type, String id, Context ctx){
        try {
            if (Bukkit.getServer().getPluginManager().isPluginEnabled("MMOItems")) {
                io.lumine.mythic.lib.api.item.NBTItem nbt = NBTItem.get(itm);
                if (nbt.getType().equalsIgnoreCase(type) && nbt.getString("MMOITEMS_ITEM_ID").equalsIgnoreCase(id)){
                    return true;
                }
                itm.getType();
            }
        }catch (Exception ex){
            ctx.debug.send(ex,null, ctx);
        }
        return false;
    }
}