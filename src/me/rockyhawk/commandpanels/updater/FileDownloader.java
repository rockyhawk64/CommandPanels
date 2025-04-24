package me.rockyhawk.commandpanels.updater;

import me.rockyhawk.commandpanels.Context;

import java.io.*;
import java.net.URL;
import java.util.logging.Level;

public class FileDownloader {

    private final Context ctx;

    public FileDownloader(Context ctx) {
        this.ctx = ctx;
    }

    public void downloadFile(String version, String pluginFileName) {
        try (
                BufferedInputStream in = new BufferedInputStream(new URL("https://github.com/rockyhawk64/CommandPanels/releases/download/" + version + "/CommandPanels.jar").openStream());
                FileOutputStream fout = new FileOutputStream(new File(new File(".").getAbsolutePath() + "/plugins/", pluginFileName))
        ) {
            ctx.plugin.getLogger().info("Downloading new update: v" + version);

            byte[] data = new byte[1024];
            long downloaded = 0L;
            int fileLength = new URL("https://github.com/rockyhawk64/CommandPanels/releases/download/" + version + "/CommandPanels.jar").openConnection().getContentLength();
            int lastPercent = 0;

            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                downloaded += count;
                fout.write(data, 0, count);
                int percent = (int) (downloaded * 100L / fileLength);
                if (percent != lastPercent && percent % 10 == 0) {
                    ctx.plugin.getLogger().info("Downloading update: " + percent + "% " + downloaded + " of " + fileLength + " bytes.");
                    lastPercent = percent;
                }
            }

            ctx.plugin.getLogger().info("Finished updating.");

        } catch (Exception e) {
            ctx.plugin.getLogger().log(Level.WARNING, "Could not download update.", e);
        }
    }
}