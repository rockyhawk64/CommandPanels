package me.rockyhawk.commandpanels.items.name;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class NameHandler {

    private final Context ctx;
    private final ItemMetaUtil itemMetaUtil;
    private final TooltipUtil tooltipUtil;
    private final LoreFormatter loreFormatter;

    public NameHandler(Context ctx) {
        this.ctx = ctx;
        this.itemMetaUtil = new ItemMetaUtil(ctx);
        this.tooltipUtil = new TooltipUtil(ctx);
        this.loreFormatter = new LoreFormatter(ctx);
    }

    public ItemStack setName(Panel panel, ItemStack item, ConfigurationSection section, Player player){
        boolean hideAttributes = false;
        boolean hideTooltip = false;
        if(section.contains("itemType")){
            //if hidden, reverse
            if(section.getStringList("itemType").contains("noAttributes")){
                hideAttributes = true;
            }
            if(section.getStringList("itemType").contains("hideTooltip")){
                hideTooltip = true;
            }
        }
        String name = section.getString("name");
        List<String> lore = section.getStringList("lore");

        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;

            name = ctx.text.placeholders(panel, PanelPosition.Top, player, name);

            if (hideAttributes) {
                itemMetaUtil.hideAttributes(meta);
            }

            if (hideTooltip) {
                tooltipUtil.hideTooltip(meta);
            }

            if (name != null) {
                meta.setDisplayName(name);
            }

            if (lore != null) {
                List<String> formattedLore = loreFormatter.format(panel, lore, player);
                meta.setLore(loreFormatter.splitListWithEscape(formattedLore));
            }

            item.setItemMeta(meta);
        } catch (Exception ignored) {
        }

        return item;
    }
}