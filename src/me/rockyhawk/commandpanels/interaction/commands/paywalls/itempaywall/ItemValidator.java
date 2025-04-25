package me.rockyhawk.commandpanels.interaction.commands.paywalls.itempaywall;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemValidator {

    private final Context ctx;

    public ItemValidator(Context ctx) {
        this.ctx = ctx;
    }

    public ItemStack createItemFromArgs(String[] args, Panel panel, PanelPosition pos, Player player) {
        ItemStack sellItem;
        if (Material.matchMaterial(args[0]) == null) {
            sellItem = ctx.itemCreate.makeCustomItemFromConfig(panel, pos, panel.getConfig().getConfigurationSection("custom-item." + args[0]), player, true, true, false);
            sellItem.setAmount(Integer.parseInt(args[1]));
        } else {
            sellItem = new ItemStack(Material.matchMaterial(args[0]), Integer.parseInt(args[1]));
        }
        return sellItem;
    }
}