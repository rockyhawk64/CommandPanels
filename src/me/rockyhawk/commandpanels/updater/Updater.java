package me.rockyhawk.commandpanels.updater;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;

public class Updater {
    CommandPanels plugin;
    public Updater(CommandPanels pl) {
        this.plugin = pl;
    }

    //if this is set to something, it will download that version on restart
    //can be a version number, 'latest' or 'cancel'
    public String downloadVersionManually = null;
    public String catchedLatestVersion = "null";

    public String githubNewUpdate(boolean sendMessages){
        //refresh latest version
        getLatestVersion(sendMessages);

        if(plugin.getDescription().getVersion().contains("-")){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GREEN + " Running a custom version.");
            }
            return null;
        }

        //if update is true there is a new update
        boolean update = false;
        if(!catchedLatestVersion.equals(plugin.getDescription().getVersion())){
            update = true;
        }

        if(update){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GOLD + " ================================================");
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.AQUA + " An update for CommandPanels is available.");
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " Download CommandPanels " + ChatColor.GOLD + catchedLatestVersion + ChatColor.WHITE + " using the");
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.WHITE + " following command:" + ChatColor.AQUA + " /cpv latest" + ChatColor.WHITE + " and restart the server");
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GOLD + " ================================================");
            }
        }
        return catchedLatestVersion;
    }

    public void getLatestVersion(boolean sendMessages){
        //using an array allows editing while still being final
        new BukkitRunnable(){
          public void run(){
              HttpURLConnection connection;
              try {
                  connection = (HttpURLConnection) new URL("https://raw.githubusercontent.com/rockyhawk64/CommandPanels/master/resource/plugin.yml").openConnection();
                  connection.connect();
                  catchedLatestVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine().split("\\s")[1];
                  connection.disconnect();
              } catch (IOException ignore) {
                  Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Could not access github.");
              }
          }
        }.runTask(plugin);

        if(catchedLatestVersion.contains("-")){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Cannot check for update.");
            }
        }
    }

    //the pluginFileName can only be obtained from the main class
    public void autoUpdatePlugin(String pluginFileName){
        String latestVersion = githubNewUpdate(false);
        String thisVersion = plugin.getDescription().getVersion();

        if(downloadVersionManually != null) {
            if (downloadVersionManually.equals("latest")) {
                downloadFile(latestVersion, pluginFileName);
            }else{
                downloadFile(downloadVersionManually, pluginFileName);
            }
            return;
        }

        if(latestVersion.equals(thisVersion) || thisVersion.contains("-")){
            //no need to update or running custom version
            return;
        }
        if(Objects.equals(plugin.config.getString("updater.minor-updates-only"), "true")){
            //only update versions that will not break
            if(thisVersion.split("\\.")[1].equals(latestVersion.split("\\.")[1]) && thisVersion.split("\\.")[0].equals(latestVersion.split("\\.")[0])){
                //the first and second number of the version is the same, updates: [major.major.minor.minor]
                downloadFile(latestVersion,pluginFileName);
            }
        }else{
            downloadFile(latestVersion,pluginFileName);
        }
    }

    private void downloadFile(String latestVersion, String pluginFileName) {
        BufferedInputStream in = null;
        FileOutputStream fout = null;

        try {
            this.plugin.getLogger().info("Downloading new update: " + "v" + latestVersion);
            URL fileUrl = new URL("https://github.com/rockyhawk64/CommandPanels/releases/download/" + latestVersion + "/Command.Panels.jar");
            int fileLength = fileUrl.openConnection().getContentLength();
            in = new BufferedInputStream(fileUrl.openStream());
            fout = new FileOutputStream(new File(new File(".").getAbsolutePath() + "/plugins/", pluginFileName));
            byte[] data = new byte[1024];

            long downloaded = 0L;

            int count;
            while((count = in.read(data, 0, 1024)) != -1) {
                downloaded += count;
                fout.write(data, 0, count);
                int percent = (int)(downloaded * 100L / (long)fileLength);
                if (percent % 10 == 0) {
                    this.plugin.getLogger().info("Downloading update: " + percent + "% of " + fileLength + " bytes.");
                }
            }
            this.plugin.getLogger().info("Finished updating.");
        } catch (Exception var22) {
            this.plugin.getLogger().log(Level.WARNING, "Could not download update.", var22);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException var21) {
                this.plugin.getLogger().log(Level.SEVERE, null, var21);
            }

            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException var20) {
                this.plugin.getLogger().log(Level.SEVERE, null, var20);
            }

        }

    }
}
