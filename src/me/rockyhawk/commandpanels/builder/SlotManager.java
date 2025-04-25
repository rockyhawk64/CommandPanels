package me.rockyhawk.commandpanels.builder;

import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.builder.storagecontents.GetStorageContents;
import me.rockyhawk.commandpanels.builder.storagecontents.GetStorageContentsLegacy;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class SlotManager {
    private final PanelBuilder bld;

    protected HashSet<Integer> takenSlots = new HashSet<>();

    public SlotManager(PanelBuilder bld) {
        this.bld = bld;
    }

    public void populateSlots(Panel panel, Player p, PanelPosition position, int animateValue, Inventory inv) {
        ConfigurationSection config = panel.getConfig();
        Set<String> itemKeys = config.getConfigurationSection("item").getKeys(false);

        for (String itemKey : itemKeys) {
            String section = bld.ctx.has.hasSection(panel, position, config.getConfigurationSection("item." + itemKey), p);

            if (config.contains("item." + itemKey + section + ".animate" + animateValue)) {
                section += ".animate" + animateValue;
            }

            ItemStack item = bld.ctx.itemCreate.makeItemFromConfig(panel, position, config.getConfigurationSection("item." + itemKey + section), p, true, true, true);
            int slot = Integer.parseInt(bld.ctx.text.placeholdersNoColour(panel, position, p, itemKey));
            setItem(item, slot, inv, p, position);
            takenSlots.add(slot);

            // handle duplicates
            bld.itemPlacer.placeDuplicates(inv, position, p, config, itemKey, item, section);
        }
    }

    protected void setItem(ItemStack item, int slot, Inventory inv, Player p, PanelPosition position) {
        if(position == PanelPosition.Top){
            inv.setItem(slot, item);
        }else if(position == PanelPosition.Middle){
            if(slot+9 < 36) {
                p.getInventory().setItem(slot + 9, item);
            }
        }else{
            if(slot < 9) {
                p.getInventory().setItem(slot, item);
            }
        }
    }

    // For refresh compatibility
    protected void setStorageContents(Player player, ItemStack[] items) {
        if (bld.ctx.version.isBelow("1.16")) {
            new GetStorageContentsLegacy().setStorageContents(player, items);
        } else {
            new GetStorageContents().setStorageContents(player, items);
        }
    }

    protected ItemStack[] getStorageContents(Inventory inventory) {
        if (bld.ctx.version.isBelow("1.13")) {
            return new GetStorageContentsLegacy().getStorageContents(inventory);
        } else {
            return new GetStorageContents().getStorageContents(inventory);
        }
    }
}