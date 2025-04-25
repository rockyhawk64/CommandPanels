package me.rockyhawk.commandpanels.builder;

import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.Objects;

public class ItemPlacer {
    private final PanelBuilder bld;

    public ItemPlacer(PanelBuilder bld) {
        this.bld = bld;
    }

    public void placeEmptyItems(Panel panel, Player p, PanelPosition pos, Inventory inv) {
        ConfigurationSection config = panel.getConfig();

        if (!config.contains("empty")) return;

        String materialName = bld.ctx.text.placeholdersNoColour(panel, pos, p, config.getString("empty"));
        if (materialName.equals("AIR")) return;

        ItemStack empty;
        try {
            short id = config.contains("emptyID")
                    ? Short.parseShort(bld.ctx.text.placeholdersNoColour(panel, pos, p, config.getString("emptyID")))
                    : 0;

            if (config.contains("custom-item." + materialName)) {
                empty = bld.ctx.itemCreate.makeItemFromConfig(panel, pos, config.getConfigurationSection("custom-item." + materialName), p, true, true, true);
            } else {
                empty = new ItemStack(Objects.requireNonNull(Material.matchMaterial(materialName.toUpperCase())), 1, id);
                ItemMeta meta = empty.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(" ");
                    if (bld.ctx.version.isAtLeast("1.21.4")) {
                        try {
                            Method m = ItemMeta.class.getMethod("setHideTooltip", boolean.class);
                            m.invoke(meta, true);
                        } catch (Exception ignore) {}
                    }
                    empty.setItemMeta(meta);

                }
            }

            for (int i = 0; i < inv.getSize(); i++) {
                if(!bld.slotManager.takenSlots.contains(i)) {
                    bld.slotManager.setItem(empty, i, inv, p, pos);
                }
            }

        } catch (Exception e) {
            bld.ctx.debug.send(e, p, bld.ctx);
        }
    }

    public void placeDuplicates(Inventory inv, PanelPosition position, Player p, ConfigurationSection config, String itemKey, ItemStack item, String section) {
        if (!config.contains("item." + itemKey + section + ".duplicate")) return;

        String[] duplicates = config.getString("item." + itemKey + section + ".duplicate").split(",");
        for (String d : duplicates) {
            try {
                if (d.contains("-")) {
                    int start = Integer.parseInt(d.split("-")[0]);
                    int end = Integer.parseInt(d.split("-")[1]);
                    for (int i = start; i <= end; i++) {
                        if (!config.contains("item." + i)) {
                            bld.slotManager.setItem(item, i, inv, p, position);
                            bld.slotManager.takenSlots.add(i);
                        }
                    }
                } else {
                    int slot = Integer.parseInt(d);
                    if (!config.contains("item." + slot)) {
                        bld.slotManager.setItem(item, slot, inv, p, position);
                        bld.slotManager.takenSlots.add(slot);
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}