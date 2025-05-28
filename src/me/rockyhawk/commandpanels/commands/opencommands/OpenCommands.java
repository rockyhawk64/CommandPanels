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

            // Extract the root command (first word of first command)
            String firstFullCommand = panelCommands.get(0);
            String commandName = firstFullCommand.split(" ")[0]; // e.g. "home"

            // Collect all subcommand patterns (without root)
            List<String> subcommandPatterns = new ArrayList<>();
            for (String cmd : panelCommands) {
                String[] parts = cmd.split(" ", 2);
                if (parts.length == 2) {
                    subcommandPatterns.add(parts[1]); // e.g. "help", "%player%"
                } else {
                    subcommandPatterns.add(""); // no subcommand, just root
                }
            }

            // Pass subcommandPatterns to BaseCommandPanel constructor
            Command command = new BaseCommandPanel(ctx, panel, commandName, subcommandPatterns);

            // Unregister old
            Command existing = knownCommands.remove(commandName);
            if (existing != null) {
                command.unregister(commandMap);
            }

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
        if (ctx.version.isBelow("1.13")) return;
        unloadCommands();
        if (!ctx.configHandler.isTrue("config.auto-register-commands")) {
            return;
        }
        loadCommands();
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }
}
