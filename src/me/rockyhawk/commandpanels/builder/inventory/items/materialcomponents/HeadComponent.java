package me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.MaterialComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.utils.CustomHeads;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HeadComponent implements MaterialComponent {
    @Override
    public boolean isCorrectTag(String tag) {
        return tag.toLowerCase().startsWith("[head]");
    }

    @Override
    public ItemStack createItem(Context ctx, String head, Player player, PanelItem item) {
        try {
            ItemStack s;
            String headName = ctx.text.parseTextToString(player, head);
            if (ctx.text.parseTextToString(player,head).length() <= 16) {
                //if [head] username
                s = ctx.customHeads.getPlayerHeadSync(headName);
            } else {
                //custom data [head] base64
                s = ctx.customHeads.getCustomHead(headName);
            }
            return s;
        } catch (Exception var32) {
            ctx.text.sendError( player, Message.ITEM_HEAD_LOAD_FAIL, head);
        }
        return null;
    }
}
