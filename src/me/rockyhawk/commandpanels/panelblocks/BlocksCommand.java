package me.rockyhawk.commandpanels.panelblocks;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class BlocksCommand implements CommandExecutor {
    Context ctx;
    public BlocksCommand(Context pl) { this.ctx = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if(args.length >= 2) {
                if (args[0].equalsIgnoreCase("add")) {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Please execute command as a Player!"));
                        return true;
                    }
                    Player p = (Player)sender;
                    if(p.hasPermission("commandpanel.block.add")){
                        if(!ctx.configHandler.isTrue("config.panel-blocks")) {
                            sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Panel blocks disabled in config!"));
                            return true;
                        }
                        boolean foundPanel = false;
                        for(Panel temp : ctx.plugin.panelList){
                            if(temp.getName().equals(args[1])){
                                foundPanel = true;
                                break;
                            }
                        }
                        if(!foundPanel){
                            sender.sendMessage(ctx.tex.colour(ctx.tag + ctx.configHandler.config.getString("config.format.nopanel")));
                            return true;
                        }
                        Block blockType = p.getTargetBlock(null, 5);
                        if(blockType.getType() == Material.AIR){
                            sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Look at a block to add a panel!"));
                            return true;
                        }
                        Location blockLocation = blockType.getLocation();
                        String configValue = "blocks." + Objects.requireNonNull(blockLocation.getWorld()).getName().replaceAll("_", "%dash%") + "_" + blockLocation.getBlockX() + "_" + blockLocation.getBlockY() + "_" + blockLocation.getBlockZ() + ".panel";
                        //this is simply getting all of the args values after the add
                        String panelValue = String.join(" ", args).replace("add ", "");
                        ctx.configHandler.blockConfig.set(configValue, panelValue);
                        try {
                            ctx.configHandler.blockConfig.save(new File(ctx.plugin.getDataFolder() + File.separator + "blocks.yml"));
                        } catch (IOException e) {
                            ctx.debug.send(e,p, ctx);
                            sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Could not save to file!"));
                            return true;
                        }
                        //make the material name look okay
                        String coordinates = blockLocation.getBlockX() + ", " + blockLocation.getBlockY() + ", " + blockLocation.getBlockZ();
                        sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.WHITE + args[1] + ChatColor.GREEN + " will now open when right clicking a block in the coordinates " + ChatColor.WHITE + coordinates));
                    }else{
                        sender.sendMessage(ctx.tex.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
                    }
                    return true;
                }
            }
            if(args.length == 1){
                if (args[0].equalsIgnoreCase("remove")) {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Please execute command as a Player!"));
                        return true;
                    }
                    Player p = (Player)sender;
                    if(p.hasPermission("commandpanel.block.remove")){
                        if(!ctx.configHandler.isTrue("config.panel-blocks")){
                            sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Panel blocks disabled in config!"));
                            return true;
                        }
                        Block blockType = p.getTargetBlock(null, 5);
                        Location blockLocation = blockType.getLocation();
                        String configValue = "blocks." + Objects.requireNonNull(blockLocation.getWorld()).getName().replaceAll("_", "%dash%") + "_" + blockLocation.getBlockX() + "_" + blockLocation.getBlockY() + "_" + blockLocation.getBlockZ() + ".panel";
                        if(ctx.configHandler.blockConfig.contains(configValue)){
                            ctx.configHandler.blockConfig.set(configValue.replace(".panel",""), null);
                            try {
                                ctx.configHandler.blockConfig.save(new File(ctx.plugin.getDataFolder() + File.separator + "blocks.yml"));
                            } catch (IOException e) {
                                ctx.debug.send(e,p, ctx);
                                sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Could not save to file!"));
                                return true;
                            }
                            sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.GREEN + "Panel has been removed from block."));
                        }else{
                            sender.sendMessage(ctx.tex.colour(ctx.tag + ctx.configHandler.config.getString("config.format.nopanel")));
                        }
                    }else{
                        sender.sendMessage(ctx.tex.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("list")) {
                    if(sender.hasPermission("commandpanel.block.list")){
                        if(!ctx.configHandler.isTrue("config.panel-blocks")){
                            sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Panel blocks disabled in config!"));
                            return true;
                        }
                        if(ctx.configHandler.blockConfig.contains("blocks")){
                            if(Objects.requireNonNull(ctx.configHandler.blockConfig.getConfigurationSection("blocks")).getKeys(false).size() == 0){
                                sender.sendMessage(ctx.tex.colour(ctx.tag) + ChatColor.RED + "No panel blocks found.");
                                return true;
                            }
                            sender.sendMessage(ctx.tex.colour(ctx.tag) + ChatColor.DARK_AQUA + "Panel Block Locations:");
                            for (String location : Objects.requireNonNull(ctx.configHandler.blockConfig.getConfigurationSection("blocks")).getKeys(false)) {
                                sender.sendMessage(ChatColor.GREEN + location.replaceAll("_"," ") + ": " + ChatColor.WHITE + ctx.configHandler.blockConfig.getString("blocks." + location + ".panel"));
                            }
                        }else{
                            sender.sendMessage(ctx.tex.colour(ctx.tag) + ChatColor.RED + "No panel blocks found.");
                        }
                    }else{
                        sender.sendMessage(ctx.tex.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
                    }
                    return true;
                }
            }
        return true;
    }
}