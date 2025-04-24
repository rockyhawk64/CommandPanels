package me.rockyhawk.commandpanels.manager.open;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class PermissionValidator {
    private final Context ctx;

    public PermissionValidator(Context ctx) {
        this.ctx = ctx;
    }

    public boolean hasPermission(CommandSender sender, Player p, Panel panel, boolean openForOtherUser) {
        String permMessage = panel.getConfig().getString("custom-messages.perms");
        String defaultPerm = ctx.configHandler.config.getString("config.format.perms");

        if (!sender.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm"))) {
            sender.sendMessage(ctx.text.colour(ctx.tag + (permMessage != null ? permMessage : defaultPerm)));
            return false;
        }

        if (sender.hasPermission("commandpanel.other") || !openForOtherUser) {
            if (!isPanelWorldEnabled(p, panel.getConfig())) {
                sender.sendMessage(ctx.text.colour(ctx.tag + (permMessage != null ? permMessage : defaultPerm)));
                return false;
            }
            return true;
        }

        sender.sendMessage(ctx.text.colour(ctx.tag + (permMessage != null ? permMessage : defaultPerm)));
        return false;
    }

    //if panel has the world enabled
    public boolean isPanelWorldEnabled(Player p, ConfigurationSection panelConfig){
        if(panelConfig.contains("disabled-worlds")){
            return !panelConfig.getStringList("disabled-worlds").contains(p.getWorld().getName());
        }
        if(panelConfig.contains("enabled-worlds")){
            return panelConfig.getStringList("enabled-worlds").contains(p.getWorld().getName());
        }
        return true;
    }
}
