package me.rockyhawk.commandpanels.interaction.openpanel;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class CommandRegister {
    private final Context ctx;
    private final CommandMap commandMap;

    public CommandRegister(Context ctx) {
        this.ctx = ctx;
        this.commandMap = getCommandMap();
    }

    // Commands will register on panel load
    // Server will need reload for changes
    public void registerPanelCommand(String command) {
        if (commandMap == null) return;
        if (command.isEmpty()) return;

        // Check if already registered by Bukkit or this
        if (Bukkit.getPluginCommand(command) != null) return;

        PluginCommand pluginCommand = createPluginCommand(command, ctx.plugin);
        if (pluginCommand != null) {
            commandMap.register(ctx.plugin.getDescription().getName(), pluginCommand);
        }
    }

    private CommandMap getCommandMap() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            return null;
        }
    }

    private PluginCommand createPluginCommand(String name, org.bukkit.plugin.Plugin plugin) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, plugin);
        } catch (Exception e) {
            return null;
        }
    }
}