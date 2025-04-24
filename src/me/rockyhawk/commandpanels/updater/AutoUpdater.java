package me.rockyhawk.commandpanels.updater;

import me.rockyhawk.commandpanels.Context;

public class AutoUpdater {

    private final Context ctx;
    private final Updater updater;

    public AutoUpdater(Context ctx, Updater updater) {
        this.ctx = ctx;
        this.updater = updater;
    }

    public void autoUpdate(String pluginFileName) {
        if (!ctx.configHandler.isTrue("updater.update-checks")) return;

        String latestVersion = updater.cachedLatestVersion;
        String currentVersion = ctx.plugin.getDescription().getVersion();

        if (updater.downloadVersionManually != null) {
            String version = updater.downloadVersionManually.equals("latest") ? latestVersion : updater.downloadVersionManually;
            new FileDownloader(ctx).downloadFile(version, pluginFileName);
            return;
        }

        if (latestVersion.equals(currentVersion) || currentVersion.contains("-")) return;

        if (!ctx.configHandler.isTrue("updater.auto-update")) return;

        String[] currParts = currentVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");

        if (currParts[0].equals(latestParts[0]) && currParts[1].equals(latestParts[1])) {
            new FileDownloader(ctx).downloadFile(latestVersion, pluginFileName);
        }
    }
}