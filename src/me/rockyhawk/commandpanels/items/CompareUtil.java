package me.rockyhawk.commandpanels.items;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.lang.reflect.Method;

public class CompareUtil {

    /*
    The ItemStack 'one' will be used, if it doesn't have a lore for example, it won't check to see if the other does have one
    The isIdentical() function will check for the following
    Material, Name, Lore, Enchanted, Potion
    */

    Context ctx;

    public CompareUtil(Context pl) {
        ctx = pl;
    }

    @SuppressWarnings("deprecation")
    public boolean isIdentical(ItemStack one, ItemStack two, Boolean nbtCheck){
        //check material
        if (one.getType() != two.getType()) {
            return false;
        }
        if(one.hasItemMeta() != two.hasItemMeta()){
            return false;
        }
        //check for name
        try {
            if (!one.getItemMeta().getDisplayName().equals(two.getItemMeta().getDisplayName())) {
                if(one.getItemMeta().hasDisplayName()) {
                    return false;
                }
            }
        }catch(Exception ignore){}
        //check for ID 1.12.2 and below
        try {
            if (ctx.version.isBelow("1.13") &&
                    (one.getDurability() != two.getDurability())) {
                return false;
            }
        }catch(Exception ignore){}
        //check for lore
        try {
            if (!one.getItemMeta().getLore().equals(two.getItemMeta().getLore())) {
                if(one.getItemMeta().hasLore()) {
                    return false;
                }
            }
        }catch(Exception ignore){}
        //check for custom model data
        try {
            if (ctx.version.isAtLeast("1.14")){
                if (one.getItemMeta().getCustomModelData() != (two.getItemMeta().getCustomModelData())) {
                    if(one.getItemMeta().hasCustomModelData()) {
                        return false;
                    }
                }
            }
        }catch(Exception ignore){}
        //check for item model data
        try {
            if(ctx.version.isAtLeast("1.21.4")){
                try {
                    // Check if the getItemModel method exists
                    Method getItemModelMethod = ItemMeta.class.getMethod("getItemModel");
                    Method hasItemModelMethod = ItemMeta.class.getMethod("hasItemModel");

                    // Invoke it dynamically
                    if (getItemModelMethod.invoke(one.getItemMeta()) != getItemModelMethod.invoke(two.getItemMeta())) {
                        if((Boolean) hasItemModelMethod.invoke(one.getItemMeta())) {
                            return false;
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // The method does not exist in older Spigot versions
                } catch (Exception e) {
                    ctx.debug.send(e,null, ctx);
                }
            }
        }catch(Exception ignore){}
        //check for nbt
        if(nbtCheck) {
            try {
                if (!ctx.nbt.hasSameNBT(one, two)) {
                    return false;
                }
            } catch (Exception ignore) {}
        }
        //check for damage
        try {
            if(ctx.version.isBelow("1.13")){
                if(one.getDurability() != two.getDurability()) {
                    return false;
                }
            }else {
                Damageable tempOne = (Damageable) one.getItemMeta();
                Damageable tempTwo = (Damageable) two.getItemMeta();
                if(tempOne.getDamage() != tempTwo.getDamage()){
                    return false;
                }
            }
        } catch (Exception ignore) {}
        //check for potions
        try {
            //choose between legacy PotionData (pre 1.20.5) or PotionType
            if(ctx.version.isBelow("1.20.5")){
                String potionOne = ctx.potion_1_20_4.retrievePotionData(one);
                String potionTwo = ctx.potion_1_20_4.retrievePotionData(two);
                if(!potionOne.equals(potionTwo)){
                    return false;
                }
            }else{
                //post 1.20.5 compare
                PotionMeta meta1 = (PotionMeta) one.getItemMeta();
                PotionMeta meta2 = (PotionMeta) two.getItemMeta();
                if (meta1.getBasePotionType().toString().compareTo(meta2.getBasePotionType().toString()) != 0){
                    return false;
                }
            }
        }catch(Exception ignore){}
        //check for enchantments
        if(!one.getEnchantments().equals(two.getEnchantments())){
            if(!one.getEnchantments().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
