package me.rockyhawk.commandpanels.commands.opencommands;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class PriorityHandler implements Listener {
    private OpenCommands openCommands;

    public PriorityHandler(OpenCommands ext) {
        openCommands = ext;
    }

    // This listener will register commands after the server loads so that it can properly override other plugins

    @EventHandler
    public void onServerLoad(ServerLoadEvent e) {
        openCommands.registerCommands();
    }

}