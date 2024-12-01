package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.openwithitem.HotbarItemLoader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commandpanelshotbar implements CommandExecutor {
    private final HotbarItemLoader hotbarItemLoader;

    public Commandpanelshotbar(HotbarItemLoader hotbarItemLoader) {
        this.hotbarItemLoader = hotbarItemLoader;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
    
        Player player = (Player) sender;
        boolean isEnabled = hotbarItemLoader.isHotbarItemsEnabled(player);
        hotbarItemLoader.toggleHotbarItems(player);
    
        if (isEnabled) {
            player.sendMessage("Hotbar items disabled.");
        } else {
            player.sendMessage("Hotbar items enabled.");
        }
    
        return true;
    }
    
}
