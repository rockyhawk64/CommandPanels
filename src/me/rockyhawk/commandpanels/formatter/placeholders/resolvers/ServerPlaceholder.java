package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerPlaceholder implements PlaceholderResolver {

    // Cache of server status results (true/false)
    private final Map<String, Boolean> cachedStatus = new ConcurrentHashMap<>();
    // Tracks which servers are currently being checked to avoid duplicate threads
    private final Map<String, Boolean> inProgress = new ConcurrentHashMap<>();

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("server-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String ipPort = identifier.replace("server-", "");

        if (cachedStatus.containsKey(ipPort)) {
            return cachedStatus.get(ipPort) ? "true" : "false";
        } else {
            // First time checking this server, launch async ping
            if (!inProgress.containsKey(ipPort)) {
                inProgress.put(ipPort, true);
                Bukkit.getScheduler().runTaskAsynchronously(ctx.plugin, () -> {
                    boolean result = pingServer(ipPort);
                    cachedStatus.put(ipPort, result);
                    inProgress.remove(ipPort);
                });
            }

            // Placeholder is still being resolved, return identifier or a fallback
            return "pinging";
        }
    }

    private boolean pingServer(String ipPort) {
        String[] parts = ipPort.split(":");
        if (parts.length != 2) return false;
        String ip = parts[0];
        int port;

        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}