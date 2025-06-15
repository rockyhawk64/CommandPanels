package me.rockyhawk.commandpanels.manager.autosave;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoSaveManager {
    private final Context ctx;
    private BukkitTask task = null;
    private boolean enabled = false;
    private long intervalTicks = 0;

    public AutoSaveManager(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Initialize the autosave system based on config settings
     */
    public void initialize() {
        // Stop any existing task
        stop();
        
        // Check if autosave is enabled
        enabled = ctx.configHandler.config.getBoolean("config.autosave.enabled", false);
        if (!enabled) {
            return;
        }

        // Parse the interval
        String intervalString = ctx.configHandler.config.getString("config.autosave.interval", "30m");
        intervalTicks = parseIntervalToTicks(intervalString);
        
        if (intervalTicks <= 0) {
            Bukkit.getLogger().warning("[CommandPanels] Invalid autosave interval: " + intervalString + ". Autosave disabled.");
            enabled = false;
            return;
        }

        // Start the autosave task
        start();
        
        Bukkit.getLogger().info("[CommandPanels] Autosave enabled with interval: " + intervalString + " (" + intervalTicks + " ticks)");
    }

    /**
     * Start the autosave task
     */
    private void start() {
        if (!enabled || intervalTicks <= 0) {
            return;
        }

        // Use runTaskTimer for repeating tasks, then run the actual save asynchronously
        task = ctx.scheduler.runTaskTimer(() -> {
            // Run the actual save operation asynchronously to avoid blocking
            ctx.scheduler.runTaskAsynchronously(() -> {
                try {
                    // Save data files (not inventory files as requested)
                    if (ctx.panelData != null) {
                        ctx.panelData.saveDataFile();
                        Bukkit.getLogger().info("[CommandPanels] Autosave: Data file saved successfully");
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().severe("[CommandPanels] Autosave failed: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }, intervalTicks, intervalTicks);
    }

    /**
     * Stop the autosave task
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Parse interval string to ticks
     * Supports: s (seconds), m (minutes), h (hours), d (days)
     * Examples: "30s", "5m", "2h", "1d"
     */
    private long parseIntervalToTicks(String interval) {
        if (interval == null || interval.trim().isEmpty()) {
            return 0;
        }

        // Pattern to match number followed by time unit
        Pattern pattern = Pattern.compile("^(\\d+)([smhd])$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(interval.trim().toLowerCase());

        if (!matcher.matches()) {
            return 0;
        }

        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);

        long seconds;
        switch (unit) {
            case "s":
                seconds = amount;
                break;
            case "m":
                seconds = amount * 60;
                break;
            case "h":
                seconds = amount * 60 * 60;
                break;
            case "d":
                seconds = amount * 60 * 60 * 24;
                break;
            default:
                return 0;
        }

        // Convert seconds to ticks (20 ticks = 1 second)
        return seconds * 20;
    }

    /**
     * Check if autosave is currently enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the current interval in ticks
     */
    public long getIntervalTicks() {
        return intervalTicks;
    }

    /**
     * Manually trigger a save (useful for commands or other triggers)
     */
    public void forceSave() {
        ctx.scheduler.runTaskAsynchronously(() -> {
            try {
                if (ctx.panelData != null) {
                    ctx.panelData.saveDataFile();
                    Bukkit.getLogger().info("[CommandPanels] Manual save: Data file saved successfully");
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("[CommandPanels] Manual save failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
} 