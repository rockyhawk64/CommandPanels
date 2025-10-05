package me.rockyhawk.commandpanels.session.inventory.listeners;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandRunner;
import me.rockyhawk.commandpanels.interaction.commands.RequirementRunner;
import me.rockyhawk.commandpanels.session.ClickActions;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ClickEvents implements Listener {

    private final Context ctx;
    public CommandRunner commands;
    public RequirementRunner requirements;

    public ClickEvents(Context ctx) {
        this.ctx = ctx;
        commands = new CommandRunner(ctx);
        requirements = new RequirementRunner(ctx);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null) return;
        if (!(e.getClickedInventory().getHolder() instanceof InventoryPanel panel)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        NamespacedKey baseIdKey = new NamespacedKey(ctx.plugin, "base_item_id");

        if (!container.has(baseIdKey, PersistentDataType.STRING)) return;

        // Cancel interaction and prevent taking the item
        e.setCancelled(true);
        e.setResult(Event.Result.DENY);

        String itemId = container.get(baseIdKey, PersistentDataType.STRING);

        // Check valid interaction types
        switch (e.getClick()) {
            case LEFT, RIGHT, MIDDLE, SHIFT_LEFT, SHIFT_RIGHT -> {
                PanelItem panelItem = panel.getItems().get(itemId);
                ClickActions actions = panelItem.getClickActions(e.getClick());
                if(!requirements.processRequirements(panel, player, actions.requirements())){
                    commands.runCommands(panel, player, actions.fail());
                    return;
                }
                commands.runCommands(panel, player, actions.commands());
            }
            default -> {
                // Ignore others
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if(!(topInventory.getHolder() instanceof InventoryPanel)) return;

        // Only care about drag targets in the top inventory
        int topSize = topInventory.getSize();
        boolean draggingOverPanelItem = event.getRawSlots().stream()
                .filter(slot -> slot < topSize) // only slots in the top inventory
                .anyMatch(slot -> {
                    var item = topInventory.getItem(slot);
                    if (item == null || !item.hasItemMeta()) return false;
                    var meta = item.getItemMeta();
                    return meta.getPersistentDataContainer().has(
                            new NamespacedKey(ctx.plugin, "item_id"), PersistentDataType.STRING
                    );
                });

        if (draggingOverPanelItem) {
            event.setCancelled(true);
        }
    }


}