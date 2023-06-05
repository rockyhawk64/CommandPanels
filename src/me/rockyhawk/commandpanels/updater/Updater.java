package me.rockyhawk.commandpanels.updater;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;

public class Updater implements Listener {
    //if this is set to something, it will download that version on restart
    //can be a version number, 'latest' or 'cancel'
    public String downloadVersionManually = null;
    public String cachedLatestVersion = "null";

    CommandPanels plugin;
    public Updater(CommandPanels pl) {
        this.plugin = pl;
    }

    //send update message when the player joins the game with the permission
    @EventHandler
    public void joinGame(PlayerJoinEvent e){
        if(e.getPlayer().hasPermission("commandpanel.update") && plugin.config.getBoolean("updater.update-checks")){
            if(githubNewUpdate(false)){
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.tex.sendMessage(e.getPlayer(),ChatColor.YELLOW + "A new update is available for download!");
                        plugin.tex.sendString(e.getPlayer(),ChatColor.YELLOW
                                + "Current version "
                                + ChatColor.RED + plugin.getDescription().getVersion() + ChatColor.YELLOW
                                + " Latest version " + ChatColor.GREEN + cachedLatestVersion);
                        this.cancel();
                    }
                }.runTaskTimer(plugin, 30, 1); //20 ticks == 1 second
            }
        }
    }

    public boolean githubNewUpdate(boolean sendMessages){
        //refresh latest version
        getLatestVersion(sendMessages);

        if(plugin.getDescription().getVersion().contains("-")){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GREEN + " Running a custom version.");
            }
            return false;
        }

        //if update is true there is a new update
        boolean update = !cachedLatestVersion.equals(plugin.getDescription().getVersion());

        if(update){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GOLD + " ================================================");
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.AQUA + " An update for CommandPanels is available.");
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " Download CommandPanels " + ChatColor.GOLD + cachedLatestVersion + ChatColor.WHITE + " using the");
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.WHITE + " following command:" + ChatColor.AQUA + " /cpv latest" + ChatColor.WHITE + " and restart the server");
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GOLD + " ================================================");
            }
            return true;
        }
        return false;
    }

    public String getLatestVersion(boolean sendMessages){
        //check for null
        if(cachedLatestVersion.equals("null")){
            cachedLatestVersion = plugin.getDescription().getVersion();
        }

        //using an array allows editing while still being final
        new BukkitRunnable(){
          public void run(){
              HttpURLConnection connection;
              try {
                  connection = (HttpURLConnection) new URL("https://raw.githubusercontent.com/rockyhawk64/CommandPanels/master/resource/plugin.yml").openConnection();
                  connection.setConnectTimeout(5000); // 5 seconds
                  connection.setReadTimeout(5000); // 5 seconds
                  connection.connect();
                  cachedLatestVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine().split("\\s")[1];
                  connection.disconnect();
              } catch (IOException ignore) {
                  Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Could not access github.");
              }
          }
        }.runTask(plugin);

        if(cachedLatestVersion.contains("-")){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Cannot check for update.");
            }
        }
        return cachedLatestVersion;
    }

    //the pluginFileName can only be obtained from the main class
    public void autoUpdatePlugin(String pluginFileName){
        if (Objects.requireNonNull(plugin.config.getString("updater.update-checks")).equalsIgnoreCase("false")) {
            return;
        }

        String latestVersion = cachedLatestVersion;
        String thisVersion = plugin.getDescription().getVersion();

        //manual download, only if it was requested
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
        if (Objects.requireNonNull(plugin.config.getString("updater.auto-update")).equalsIgnoreCase("false")) {
            //return if auto-update is false
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
            URL fileUrl = new URL("https://github.com/rockyhawk64/CommandPanels/releases/download/" + latestVersion + "/CommandPanels.jar");
            int fileLength = fileUrl.openConnection().getContentLength();
            in = new BufferedInputStream(fileUrl.openStream());
            fout = new FileOutputStream(new File(new File(".").getAbsolutePath() + "/plugins/", pluginFileName));
            byte[] data = new byte[1024];

            long downloaded = 0L;

            int count;
            int lastpercent = 0;
            while((count = in.read(data, 0, 1024)) != -1) {
                downloaded += count;
                fout.write(data, 0, count);
                int percent = (int)(downloaded * 100L / (long)fileLength);
                if (percent != lastpercent && percent % 10 == 0) {
                    //show if the percentage is different from the last percentage and then show the bytes downloaded so far
                    this.plugin.getLogger().info("Downloading update: " + percent + "% " + downloaded + " of " + fileLength + " bytes.");
                    lastpercent = percent;
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
