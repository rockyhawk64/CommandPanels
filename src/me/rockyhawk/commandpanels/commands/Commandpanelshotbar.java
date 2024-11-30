package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandpanelsHotbar implements CommandExecutor {
    private final HotbarItemLoader hotbarItemLoader;

    // Der Konstruktorname muss dem Klassennamen entsprechen
    public CommandpanelsHotbar(HotbarItemLoader hotbarItemLoader) {
        this.hotbarItemLoader = hotbarItemLoader;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        boolean status = hotbarItemLoader.toggleHotbarItems(player);

        if (status) {
            player.sendMessage("Hotbar items enabled.");
        } else {
            player.sendMessage("Hotbar items disabled.");
        }

        return true;
    }
}
