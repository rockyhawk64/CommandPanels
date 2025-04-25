package me.rockyhawk.commandpanels.items.builder.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.ItemComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class PotionComponent implements ItemComponent {

    @Override
    public ItemStack apply(ItemStack item, ConfigurationSection section, Context ctx, Player player, Panel panel, PanelPosition pos, boolean addNBT) {
        if(!section.contains("potion")) return item;

        //if the item is a potion, give it an effect
        String[] effectType = ctx.text.placeholdersNoColour(panel,pos,player,section.getString("potion")).split("\\s");
        //potion legacy or current
        if(ctx.version.isBelow("1.20.5")){
            if(ctx.version.isBelow("1.9")){
                ctx.potion_1_8.applyPotionEffect(player, item, effectType);
            }else {
                ctx.potion_1_20_4.applyPotionEffect(player, item, effectType);
            }
        }else{
            try {
                PotionMeta potionMeta = (PotionMeta)item.getItemMeta();
                assert potionMeta != null;
                PotionType newData = PotionType.valueOf(effectType[0].toUpperCase());
                //set meta
                potionMeta.setBasePotionType(newData);
                item.setItemMeta(potionMeta);
            } catch (Exception er) {
                //don't add the effect
                ctx.debug.send(er,player, ctx);
                player.sendMessage(ctx.text.colour(ctx.tag + ChatColor.RED + ctx.configHandler.config.getString("config.format.error") + " potion: " + section.getString("potion")));
            }
        }

        return item;
    }
}