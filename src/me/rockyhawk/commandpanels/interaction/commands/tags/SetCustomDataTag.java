package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;

public class SetCustomDataTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.startsWith("setcustomdata=")) return false;

        String[] args = ctx.text.attachPlaceholders(panel, pos, player, command).split("\\s+");
        PanelPosition position = PanelPosition.valueOf(args[2]);
        ItemStack editItem;

        if (position == PanelPosition.Top) {
            editItem = player.getOpenInventory().getTopInventory().getItem(Integer.parseInt(args[1]));
        } else if (position == PanelPosition.Middle) {
            editItem = player.getInventory().getItem(Integer.parseInt(args[1]) + 9);
        } else {
            editItem = player.getInventory().getItem(Integer.parseInt(args[1]));
        }

        try {
            ItemMeta itemMeta = editItem.getItemMeta();
            if(ctx.version.isAtLeast("1.21.4")){
                editItem.setItemMeta(itemModel(itemMeta, args[3]));
            }else{
                itemMeta.setCustomModelData(Integer.valueOf(args[3]));
            }
            editItem.setItemMeta(itemMeta);
        } catch (Exception err) {
            ctx.debug.send(err, player, ctx);
        }

        return true;
    }

    //Item Model 1.21.4+
    private ItemMeta itemModel(ItemMeta itemMeta, String itemModel) throws Exception {
        // Check if the setHideTooltip method exists
        Method setItemModelMethod;
        setItemModelMethod = ItemMeta.class.getMethod("setItemModel", NamespacedKey.class);
        // Invoke it dynamically
        setItemModelMethod.invoke(itemMeta, NamespacedKey.fromString(itemModel));
        return itemMeta;
    }
}