package me.rockyhawk.commandpanels.items.potions;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ClassicPotionData {

    public void applyPotionEffect(Player p, ItemStack item, String[] effectType) {
        try {
            if (item == null || item.getType() != org.bukkit.Material.POTION) {
                return;
            }

            Class<?> potionClass = Class.forName("org.bukkit.potion.Potion");
            Class<?> potionTypeClass = Class.forName("org.bukkit.potion.PotionType");

            Object potionType = Enum.valueOf((Class<Enum>) potionTypeClass, effectType[0].toUpperCase());
            Constructor<?> potionConstructor = potionClass.getConstructor(potionTypeClass, int.class);
            Object potion = potionConstructor.newInstance(potionType, effectType.length >= 3 && effectType[2].equalsIgnoreCase("true") ? 2 : 1);

            Method setSplashMethod = potionClass.getMethod("setSplash", boolean.class);
            boolean isSplash = (item.getDurability() & 0x4000) != 0; // Checks if the durability indicates it's a splash potion
            setSplashMethod.invoke(potion, isSplash);

            try {
                Method setHasExtendedDurationMethod = potionClass.getMethod("setHasExtendedDuration", boolean.class);
                setHasExtendedDurationMethod.invoke(potion, effectType.length >= 2 && effectType[1].equalsIgnoreCase("true"));
            }catch (Exception ignore){
                //ignore as some potions like instant potions cannot be extended
            }

            Method applyMethod = potionClass.getMethod("apply", ItemStack.class);
            applyMethod.invoke(potion, item);
        } catch (Exception er) {
            p.sendMessage(ChatColor.RED + "Incorrect potion type or format.");
        }
    }

    public String retrievePotionData(ItemStack item) {
        try {
            if (item == null || item.getType() != org.bukkit.Material.POTION) {
                return "Item is not a potion";
            }

            Class<?> potionClass = Class.forName("org.bukkit.potion.Potion");
            Method fromItemStackMethod = potionClass.getMethod("fromItemStack", ItemStack.class);
            Object potion = fromItemStackMethod.invoke(null, item);

            Method getTypeMethod = potionClass.getMethod("getType");
            Method isExtendedMethod = potionClass.getMethod("hasExtendedDuration");
            Method isUpgradedMethod = potionClass.getMethod("getLevel");

            Object potionType = getTypeMethod.invoke(potion);
            boolean extended = (Boolean) isExtendedMethod.invoke(potion);
            boolean upgraded = ((Integer) isUpgradedMethod.invoke(potion)) > 1;

            boolean isSplash = (item.getDurability() & 0x4000) != 0; // Checks if the durability indicates it's a splash potion

            return potionType.toString() + " " + extended + " " + upgraded + " Splash:" + isSplash;
        } catch (Exception e) {
            return "Failed to retrieve potion data";
        }
    }
}
