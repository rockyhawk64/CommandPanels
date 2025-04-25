package me.rockyhawk.commandpanels.items.builder.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.ItemComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class DamageComponent implements ItemComponent {

    @Override
    public ItemStack apply(ItemStack item, ConfigurationSection section, Context ctx, Player player, Panel panel, PanelPosition pos, boolean addNBT) {
        if(!section.contains("damage")) return item;

        //change the damage amount (placeholders accepted)
        //if the damage is not unbreakable and should be a value
        if(ctx.version.isBelow("1.13")){
            try {
                item.setDurability(Short.parseShort(Objects.requireNonNull(ctx.text.placeholders(panel,pos,player, section.getString("damage")))));
            } catch (Exception e) {
                ctx.debug.send(e, player, ctx);
                player.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.error") + " damage: " + section.getString("damage")));
            }
        } else {
            if(section.getString("damage").equalsIgnoreCase("-1")){
                //if the player wants the item to be unbreakable. Only works in non legacy versions
                ItemMeta unbreak = item.getItemMeta();
                unbreak.setUnbreakable(true);
                item.setItemMeta(unbreak);
            }else {
                try {
                    Damageable itemDamage = (Damageable) item.getItemMeta();
                    itemDamage.setDamage(Integer.parseInt(Objects.requireNonNull(ctx.text.placeholders(panel, pos, player, section.getString("damage")))));
                    item.setItemMeta(itemDamage);
                } catch (Exception e) {
                    ctx.debug.send(e, player, ctx);
                    player.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.error") + " damage: " + section.getString("damage")));
                }
            }
        }

        return item;
    }
}