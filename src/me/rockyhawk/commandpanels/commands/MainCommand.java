package me.rockyhawk.commandpanels.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.commands.subcommands.*;
import me.rockyhawk.commandpanels.formatter.language.Message;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MainCommand implements BasicCommand {
    private final Context ctx;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public MainCommand(Context ctx) {
        this.ctx = ctx;
        registerSubCommands();
    }

    private void registerSubCommands() {
        register(new ReloadCommand());
        register(new OpenCommand());
        register(new GenerateCommand());
        register(new HelpCommand());
        register(new VersionCommand());
        register(new DataCommand());
    }

    private void register(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public void execute(CommandSourceStack cmdStack, String[] args){
        if (!cmdStack.getSender().hasPermission("commandpanels.command")) {
            ctx.text.sendError(cmdStack.getSender(), Message.COMMAND_NO_PERMISSION);
            return;
        }

        if (args.length == 0) {
            ctx.text.sendError(cmdStack.getSender(), Message.COMMAND_SUBCOMMAND_HELP);
            return;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand == null) {
            ctx.text.sendError(cmdStack.getSender(), Message.COMMAND_UNKNOWN_SUBCOMMAND);
            return;
        }

        if (subCommand.getPermission() != null && !subCommand.getPermission().isEmpty() && !cmdStack.getSender().hasPermission(subCommand.getPermission())) {
            ctx.text.sendError(cmdStack.getSender(), Message.COMMAND_NO_PERMISSION);
            return;
        }

        // Pass the remaining args after the subcommand name
        String[] subArgs = args.length > 1 ? java.util.Arrays.copyOfRange(args, 1, args.length) : new String[0];
        subCommand.execute(ctx, cmdStack.getSender(), subArgs);
    }

    @Override
    public @Nullable String permission() {
        return "commandpanels.command";
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        TabComplete suggestions = new TabComplete(ctx);
        return suggestions.onTabComplete(commandSourceStack.getSender(), args);
    }
}