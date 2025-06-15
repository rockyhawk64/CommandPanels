package me.rockyhawk.commandpanels.manager.open;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import me.rockyhawk.commandpanels.api.events.PanelOpenedEvent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public class OpenPanel {
    public final PermissionValidator permission;
    private final Context ctx;
    private final PanelCommandExecutor commandExecutor;
    private final SoundHandler soundPlayer;
    private final PreLoadCommands preloader;
    private final OpenRequirements requirementsValidator;

    public OpenPanel(Context ctx) {
        this.ctx = ctx;
        this.permission = new PermissionValidator(ctx);
        this.commandExecutor = new PanelCommandExecutor(ctx);
        this.soundPlayer = new SoundHandler(ctx);
        this.preloader = new PreLoadCommands(ctx);
        this.requirementsValidator = new OpenRequirements(ctx);
    }

    public void open(CommandSender sender, Player p, Panel panel, PanelPosition position) {
        if (p == null) {
            sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.RED + "Player not found."));
            return;
        }
        if (p.isSleeping()) return;

        if ((ctx.debug.isEnabled(sender) || ctx.configHandler.isTrue("config.auto-update-panels")) && panel.getFile() != null) {
            panel.setConfig(YamlConfiguration.loadConfiguration(panel.getFile()));
        }

        boolean openForOtherUser = !(sender instanceof Player && sender == p);

        if (!permission.hasPermission(sender, p, panel, position, openForOtherUser)) return;

        // Check open requirements before allowing panel to open
        if (!requirementsValidator.canOpenPanel(panel, p, position)) {
            String failMessage = requirementsValidator.getOpenRequirementFailMessage(panel);
            sender.sendMessage(ctx.text.colour(ctx.tag + failMessage));
            return;
        }

        if (position != PanelPosition.Top && !ctx.openPanels.hasPanelOpen(p.getName(), PanelPosition.Top)) {
            sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.RED + "Cannot open a panel without a panel at the top already."));
            return;
        }

        if (!ctx.openPanels.hasPanelOpen(p.getName(), PanelPosition.Top) && p.getOpenInventory().getType() != InventoryType.CRAFTING) {
            p.closeInventory();
        }

        PanelOpenedEvent openedEvent = new PanelOpenedEvent(p, panel, position);
        Bukkit.getPluginManager().callEvent(openedEvent);
        if (openedEvent.isCancelled()) return;

        preloader.executePreLoad(panel, position, p);

        try {
            new PanelBuilder(ctx).openInv(panel, p, position, 0);
            commandExecutor.executeOpenCommands(panel, position, p);
            soundPlayer.playOpenSound(panel, p);

            if (openForOtherUser) {
                sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.GREEN + "Panel Opened for " + p.getDisplayName()));
            }
        } catch (Exception r) {
            ctx.debug.send(r, null, ctx);
            sender.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.error")));
            ctx.openPanels.closePanelForLoader(p.getName(), position);
            p.closeInventory();
        }
    }
}