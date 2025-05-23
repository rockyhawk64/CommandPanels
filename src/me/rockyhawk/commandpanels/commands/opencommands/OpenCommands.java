package me.rockyhawk.commandpanels.commands.opencommands;

import com.google.common.collect.Maps;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenCommands {

    private final Context ctx;
    private final Map<String, Command> commands = Maps.newHashMap();
    private final Map<String, Command> knownCommands = getKnownCommands();
    private final CommandMap commandMap = getCommandMap();

    private Map<String, Command> getKnownCommands() {
        try {
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            return (Map<String, Command>) knownCommandsField.get(getCommandMap());
        } catch (Exception e) {
            throw new RuntimeException("Could not get known commands", e);
        }
    }

    public OpenCommands(Context pl) {
        this.ctx = pl;
    }

    private void unloadCommands() {
        commands.forEach((commandName, command) -> unregisterCommand(command));
    }

    public void unregisterCommand(Command command) {
        try {
            command.getAliases().forEach(knownCommands::remove);
            knownCommands.remove(command.getName());
            command.unregister(commandMap);
        } catch (Exception ignored) {}
    }

    private void loadCommands() {
        ctx.plugin.panelList.forEach(panel -> {
            if (!panel.getConfig().contains("commands")) {
                return;
            }

            List<String> panelCommands = panel.getConfig().getStringList("commands");
            if (panelCommands.isEmpty()) {
                return;
            }

            String commandName = panelCommands.remove(0);
            Command command = new BaseCommandPanel(ctx, panel, commandName, panelCommands);

            commandMap.register(command.getName(), "commandpanels", command);
            commands.put(command.getName(), command);
        });
    }

    private CommandMap getCommandMap() {
        try {
            final Class<? extends Server> serverClass = Bukkit.getServer().getClass();
            final Field commandMapField = serverClass.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            throw new RuntimeException("Unable to get command map", e);
        }
    }


    public void registerCommands(){
        unloadCommands();
        loadCommands();
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }
}
