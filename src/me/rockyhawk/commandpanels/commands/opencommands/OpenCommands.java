package me.rockyhawk.commandpanels.commands.opencommands;

import com.google.common.collect.Maps;
import me.rockyhawk.commandpanels.Context;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
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
        if(ctx.version.isAtLeast("1.13")){
            Bukkit.getServer().getPluginManager().registerEvents(new PriorityHandler(this), ctx.plugin);
        }
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

            // Group commands by their root command (first word)
            Map<String, List<String>> commandsByRoot = new HashMap<>();

            for (String fullCommand : panelCommands) {
                String[] parts = fullCommand.split(" ", 2);
                String rootCommand = parts[0]; // e.g. "home"
                String subcommand = parts.length > 1 ? parts[1] : "";

                commandsByRoot.computeIfAbsent(rootCommand, k -> new ArrayList<>()).add(subcommand);
            }

            // For each root command, create and register a BaseCommandPanel
            for (Map.Entry<String, List<String>> entry : commandsByRoot.entrySet()) {
                String rootCommand = entry.getKey();
                List<String> subcommands = entry.getValue();

                Command command = new BaseCommandPanel(ctx, panel, rootCommand, subcommands);

                // Unregister old command if present
                Command existing = knownCommands.remove(rootCommand);
                if (existing != null) {
                    existing.unregister(commandMap);
                }

                commandMap.register(command.getName(), "commandpanels", command);
                commands.put(command.getName(), command);
            }
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
        if (ctx.version.isBelow("1.13")) return;
        unloadCommands();
        if (!ctx.configHandler.isTrue("config.auto-register-commands")) {
            return;
        }
        loadCommands();
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }
}
