package me.rockyhawk.commandpanels.updater;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerJoinEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionChecker {

    private final Context ctx;
    private final Updater updater;

    public VersionChecker(Context ctx, Updater updater) {
        this.ctx = ctx;
        this.updater = updater;
    }

    public void notifyUpdateOnJoin(PlayerJoinEvent e) {
        if (e.getPlayer().hasPermission("commandpanel.update") && ctx.configHandler.isTrue("updater.update-checks")) {
            if (githubNewUpdate(false)) {
                ctx.scheduler.runTaskLaterForEntity(e.getPlayer(), () -> {
                    ctx.text.sendMessage(e.getPlayer(), ChatColor.YELLOW + "A new update is available for download!");
                    ctx.text.sendString(e.getPlayer(), ChatColor.YELLOW
                            + "Current version "
                            + ChatColor.RED + ctx.plugin.getDescription().getVersion() + ChatColor.YELLOW
                            + " Latest version " + ChatColor.GREEN + updater.cachedLatestVersion);
                }, 30L);
            }
        }
    }

    public boolean githubNewUpdate(boolean sendMessages) {
        getLatestVersion(sendMessages);

        String currentVersion = ctx.plugin.getDescription().getVersion();

        if (currentVersion.contains("SNAPSHOT")) {
            if (sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GREEN + " Running a custom version.");
            }
            return false;
        }

        boolean update = !updater.cachedLatestVersion.equals(currentVersion);

        if (update && sendMessages) {
            Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GOLD + " ================================================");
            Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.AQUA + " An update for CommandPanels is available.");
            Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " Download CommandPanels " + ChatColor.GOLD + updater.cachedLatestVersion + ChatColor.WHITE + " using the");
            Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.WHITE + " following command:" + ChatColor.AQUA + " /cpv latest" + ChatColor.WHITE + " and restart the server");
            Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GOLD + " ================================================");
        }

        return update;
    }

    public String getLatestVersion(boolean sendMessages) {
        if (updater.cachedLatestVersion.equals("null")) {
            updater.cachedLatestVersion = ctx.plugin.getDescription().getVersion();
        }

        ctx.scheduler.runTaskAsynchronously(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("https://raw.githubusercontent.com/rockyhawk64/CommandPanels/latest/resource/plugin.yml").openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                updater.cachedLatestVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine().split("\\s")[1];
                connection.disconnect();
            } catch (IOException ignored) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Could not access github.");
            }
        });

        if (updater.cachedLatestVersion.contains("-") && sendMessages) {
            Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Cannot check for update.");
        }

        return updater.cachedLatestVersion;
    }
}
