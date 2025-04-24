package me.rockyhawk.commandpanels.updater;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Updater implements Listener {
    public String downloadVersionManually = null;
    public String cachedLatestVersion = "null";
    public final VersionChecker versionChecker;

    private final AutoUpdater autoUpdater;

    public Updater(Context ctx) {
        this.versionChecker = new VersionChecker(ctx, this);
        this.autoUpdater = new AutoUpdater(ctx, this);

        if (ctx.configHandler.isTrue("updater.update-checks")) {
            versionChecker.githubNewUpdate(true);
        }
    }

    @EventHandler
    public void joinGame(PlayerJoinEvent e) {
        versionChecker.notifyUpdateOnJoin(e);
    }

    public void autoUpdatePlugin(String pluginFileName) {
        autoUpdater.autoUpdate(pluginFileName);
    }
}