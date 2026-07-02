package me.rockyhawk.commandpanels.session.inventory.listeners;

import io.papermc.paper.persistence.PersistentDataContainerView;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandRunner;
import me.rockyhawk.commandpanels.interaction.commands.RequirementRunner;
import me.rockyhawk.commandpanels.session.CommandActions;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ClickEvents implements Listener {

    private final Context ctx;
    public CommandRunner commands;
    public RequirementRunner requirements;

    // Cached keys: NamespacedKey construction validates its args via regex every call,
    private final NamespacedKey baseIdKey;
    private final NamespacedKey itemIdKey;
    private final NamespacedKey lastClickKey;

    private static final long CLICK_COOLDOWN_MILLIS = 100L;

    public ClickEvents(Context ctx) {
        this.ctx = ctx;
        commands = new CommandRunner(ctx);
        requirements = new RequirementRunner(ctx);
        this.baseIdKey = new NamespacedKey(ctx.plugin, "base_item_id");
        this.itemIdKey = new NamespacedKey(ctx.plugin, "item_id");
        this.lastClickKey = new NamespacedKey(ctx.plugin, "last_click_time");
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null) return;
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof InventoryPanel panel)) return;
        if (e.getClickedInventory() != e.getView().getBottomInventory()) return;

        // Cancel player inventory click if locked
        boolean isLocked = Boolean.parseBoolean(
                ctx.text.parseTextToString(player, panel.getInventoryLock()));
        if (isLocked) e.setCancelled(true);
    }

    @EventHandler
    public void onOutsideInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() != null) return;
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof InventoryPanel panel)) return;

        // Run outside command actions
        CommandActions actions = panel.getOutsideCommands();
        if (!requirements.processRequirements(panel, player, actions.requirements())) {
            commands.runCommands(panel, player, actions.fail());
            return;
        }
        commands.runCommands(panel, player, actions.commands());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null) return;
        if (!(e.getClickedInventory().getHolder() instanceof InventoryPanel panel)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null) return;

        // Check if item has commandpanels data attached
        PersistentDataContainerView container = item.getPersistentDataContainer();

        String itemId = container.getOrDefault(baseIdKey, PersistentDataType.STRING, null);
        if (itemId == null) return;

        // Cancel interaction and prevent taking the item
        e.setCancelled(true);
        e.setResult(Event.Result.DENY);

        // Cooldown check first, so rejected/spammed
        // clicks bail out before touching getItemMeta() or the item's own PDC.
        PersistentDataContainer playerData = player.getPersistentDataContainer();
        Long lastClickMillis = playerData.get(lastClickKey, PersistentDataType.LONG);
        long currentMillis = System.currentTimeMillis();
        if (lastClickMillis != null && currentMillis - lastClickMillis < CLICK_COOLDOWN_MILLIS) {
            return;
        }
        playerData.set(lastClickKey, PersistentDataType.LONG, currentMillis);

        // Check valid interaction types
        switch (e.getClick()) {
            case LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT -> {
                PanelItem panelItem = panel.getItems().get(itemId);
                CommandActions actions = panelItem.getClickActions(e.getClick());
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
                    if (item == null) return false;
                    var cont = item.getPersistentDataContainer();
                    return cont.has(itemIdKey, PersistentDataType.STRING);
                });

        if (draggingOverPanelItem) {
            event.setCancelled(true);
        }
    }
}