package me.rockyhawk.commandpanels.ioclasses.potions;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class LegacyPotionData {
    /*
     * To be used in Minecraft 1.9 to 1.20.4
     * */
    private CommandPanels plugin;

    public LegacyPotionData(CommandPanels plugin) {
        this.plugin = plugin;
    }

    //effect type should be PotionType Extended? Upgraded?
    public void applyPotionEffect(Player p, ItemStack item, String[] effectType) {
        try {
            PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
            assert potionMeta != null;
            boolean extended = false;
            boolean upgraded = false;

            if (effectType.length >= 2) {
                extended = effectType[1].equalsIgnoreCase("true");
                if (effectType.length == 3) {
                    upgraded = effectType[2].equalsIgnoreCase("true");
                }
            }

            Class<?> potionDataType = Class.forName("org.bukkit.potion.PotionData");
            Constructor<?> potionDataConstructor = potionDataType.getConstructor(PotionType.class, boolean.class, boolean.class);
            Object potionData = potionDataConstructor.newInstance(PotionType.valueOf(effectType[0].toUpperCase()), extended, upgraded);

            Method setBasePotionDataMethod = potionMeta.getClass().getMethod("setBasePotionData", potionDataType);
            setBasePotionDataMethod.setAccessible(true); // Setting the method as accessible
            setBasePotionDataMethod.invoke(potionMeta, potionData);

            item.setItemMeta(potionMeta);
        } catch (Exception er) {
            plugin.debug(er, p);
            p.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + plugin.config.getString("config.format.error") + " incorrect potion"));
        }
    }

    //returns PotionType Extended? Upgraded?
    public String retrievePotionData(ItemStack item) {
        try {
            PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
            assert potionMeta != null;

            Method getBasePotionDataMethod = potionMeta.getClass().getMethod("getBasePotionData");
            getBasePotionDataMethod.setAccessible(true);  // Set the method as accessible
            Object potionData = getBasePotionDataMethod.invoke(potionMeta);

            Class<?> potionDataType = Class.forName("org.bukkit.potion.PotionData");
            Method getTypeMethod = potionDataType.getMethod("getType");
            Method isUpgradedMethod = potionDataType.getMethod("isUpgraded");
            Method isExtendedMethod = potionDataType.getMethod("isExtended");

            getTypeMethod.setAccessible(true);  // Set the method as accessible
            isUpgradedMethod.setAccessible(true);  // Set the method as accessible
            isExtendedMethod.setAccessible(true);  // Set the method as accessible

            PotionType potionType = (PotionType) getTypeMethod.invoke(potionData);
            boolean level = (boolean) isUpgradedMethod.invoke(potionData);
            boolean extended = (boolean) isExtendedMethod.invoke(potionData);

            return potionType.name() + " " + extended + " " + level;
        } catch (Exception e) {
            plugin.debug(e, null);
            return null;
        }
    }
}
