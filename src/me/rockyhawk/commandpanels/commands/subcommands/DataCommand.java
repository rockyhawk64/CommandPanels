package me.rockyhawk.commandpanels.commands.subcommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.commands.SubCommand;
import me.rockyhawk.commandpanels.formatter.language.Message;
import org.bukkit.command.CommandSender;

public class DataCommand implements SubCommand {

    @Override
    public String getName() {
        return "data";
    }

    @Override
    public String getPermission() {
        return "commandpanels.command.data";
    }

    @Override
    public boolean execute(Context ctx, CommandSender sender, String[] args) {
        if (args.length < 2) {
            ctx.text.sendError(sender, Message.DATA_USAGE);
            return true;
        }

        String action = args[0].toLowerCase();
        String playerName = args[1];

        boolean isSilent = false;
        for (String arg : args) {
            if (arg.equals("-s")) {
                isSilent = true;
                break;
            }
        }

        switch (action) {
            case "get": {
                if (args.length < 3) {
                    ctx.text.sendError(sender, Message.DATA_MISSING_KEY);
                    return true;
                }
                String key = args[2];
                String value = ctx.dataLoader.getUserData(playerName, key);
                if (!isSilent) ctx.text.sendInfo(sender, Message.DATA_VALUE, (value != null ? value : "null"));
                return true;
            }

            case "set": {
                if (args.length < 4) {
                    ctx.text.sendError(sender, Message.DATA_MISSING_KEY_OR_VALUE);
                    return true;
                }
                String key = args[2];
                String value = args[3];
                ctx.dataLoader.setUserData(playerName, key, value, false);
                if (!isSilent) ctx.text.sendInfo(sender, Message.DATA_SET, key, value);
                return true;
            }

            case "overwrite": {
                if (args.length < 4) {
                    ctx.text.sendError(sender, Message.DATA_MISSING_KEY_OR_VALUE);
                    return true;
                }
                String key = args[2];
                String value = args[3];
                ctx.dataLoader.setUserData(playerName, key, value, true);
                if (!isSilent) ctx.text.sendInfo(sender, Message.DATA_OVERWRITE, key, value);
                return true;
            }

            case "math": {
                if (args.length < 4) {
                    ctx.text.sendError(sender, Message.DATA_MISSING_KEY_OR_EXPRESSION);
                    return true;
                }
                String key = args[2];
                String expression = args[3];
                ctx.dataLoader.doDataMath(playerName, key, expression);
                if (!isSilent) ctx.text.sendInfo(sender, Message.DATA_MATH, expression, key);
                return true;
            }

            case "del": {
                if (args.length < 3) {
                    ctx.text.sendError(sender, Message.DATA_MISSING_KEY);
                    return true;
                }
                String key = args[2];
                ctx.dataLoader.delUserData(playerName, key);
                if (!isSilent) ctx.text.sendInfo(sender, Message.DATA_DELETE, key);
                return true;
            }

            case "clear": {
                ctx.dataLoader.clearData(playerName);
                if (!isSilent) ctx.text.sendInfo(sender, Message.DATA_CLEAR, playerName);
                return true;
            }

            default: {
                ctx.text.sendError(sender, Message.DATA_UNKNOWN_ACTION);
                return true;
            }
        }
    }
}