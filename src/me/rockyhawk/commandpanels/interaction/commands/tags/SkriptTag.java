package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Arrays;

public class SkriptTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.startsWith("skript=")) return false;
        
        String[] args = ctx.text.attachPlaceholders(panel, pos, player, command).split("\\s+");
        args = Arrays.copyOfRange(args, 1, args.length); // Remove first element from args
        
        if (args.length == 0) {
            player.sendMessage(ctx.tag + ctx.text.colour(ctx.configHandler.config.getString("config.format.error") + " skript=: No Skript command provided!"));
            return true;
        }
        
        // Check if Skript plugin is enabled
        if (!Bukkit.getPluginManager().isPluginEnabled("Skript")) {
            player.sendMessage(ctx.tag + ctx.text.colour(ctx.configHandler.config.getString("config.format.error") + " skript=: Skript plugin is not loaded!"));
            return true;
        }
        
        try {
            String commandName = args[0];
            String[] commandArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
            
            executeSkriptCommand(player, commandName, commandArgs);
            
        } catch (Exception ex) {
            player.sendMessage(ctx.tag + ctx.text.colour(ctx.configHandler.config.getString("config.format.error") + " skript=: Error executing Skript command!"));
            ctx.debug.send(ex, player, ctx);
        }
        
        return true;
    }
    
    // Method to execute a Skript command by name
    private void executeSkriptCommand(Player player, String commandName, String[] args) {
        try {
            // Try to get the Skript command classes
            Class<?> commandsClass = Class.forName("ch.njol.skript.command.Commands");
            Class<?> scriptCommandClass = Class.forName("ch.njol.skript.command.ScriptCommand");
            
            // Get the getCommands method
            Method getCommandsMethod = commandsClass.getMethod("getCommands");
            Object commands = getCommandsMethod.invoke(null);
            
            // Check if the returned object is iterable
            if (commands instanceof Iterable) {
                @SuppressWarnings("unchecked")
                Iterable<Object> commandList = (Iterable<Object>) commands;
                
                // Find the command in Skript's registered commands
                for (Object command : commandList) {
                    if (command.getClass().equals(scriptCommandClass)) {
                        Method getNameMethod = command.getClass().getMethod("getName");
                        String scriptCommandName = (String) getNameMethod.invoke(command);
                        
                        if (scriptCommandName.equalsIgnoreCase(commandName)) {
                            // Execute the command for the player with given arguments
                            Method executeMethod = command.getClass().getMethod("execute", Player.class, String.class, String[].class);
                            executeMethod.invoke(command, player, commandName, args);
                            return;
                        }
                    }
                }
            }
            
            player.sendMessage("Command '" + commandName + "' not found in Skript!");
            
        } catch (Exception e) {
            // If the direct approach fails, try alternative method using functions
            try {
                executeSkriptFunction(commandName, new Object[]{player});
            } catch (Exception ex) {
                // Fallback: dispatch as regular command with skript prefix
                String fullCommand = commandName;
                if (args.length > 0) {
                    fullCommand += " " + String.join(" ", args);
                }
                
                // Try to execute as a regular command (in case it's a custom Skript command)
                Bukkit.dispatchCommand(player, fullCommand);
            }
        }
    }
    
    // Execute a Skript function as an alternative approach
    private void executeSkriptFunction(String functionName, Object[] params) throws Exception {
        Class<?> functionsClass = Class.forName("ch.njol.skript.lang.function.Functions");
        Class<?> functionClass = Class.forName("ch.njol.skript.lang.function.Function");
        
        Method getFunctionMethod = functionsClass.getMethod("getFunction", String.class);
        Object function = getFunctionMethod.invoke(null, functionName);
        
        if (function != null) {
            Method executeMethod = functionClass.getMethod("execute", Object[].class);
            executeMethod.invoke(function, new Object[]{params});
        }
    }
} 