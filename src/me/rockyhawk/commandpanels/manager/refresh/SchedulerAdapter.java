package me.rockyhawk.commandpanels.manager.refresh;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Scheduler adapter that provides compatibility between Bukkit/Spigot and Folia
 * Based on direct API access rather than reflection for better reliability
 */
public class SchedulerAdapter {
    private final Plugin plugin;
    private final boolean isFolia;
    
    public SchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
        this.isFolia = isFoliaServer();
    }
    
    /**
     * Detects if the server is running Folia
     */
    private boolean isFoliaServer() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Runs a task synchronously
     */
    public BukkitTask runTask(Runnable task) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
            return new DummyBukkitTask();
        } else {
            return Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * Runs a task synchronously with a delay
     */
    public BukkitTask runTaskLater(Runnable task, long delay) {
        if (isFolia) {
            // Folia's GlobalRegionScheduler.runDelayed() uses ticks, not milliseconds!
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay);
            return new DummyBukkitTask();
        } else {
            return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * Runs a task synchronously for a specific entity
     */
    public BukkitTask runTaskForEntity(Entity entity, Runnable task) {
        if (isFolia) {
            entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
            return new DummyBukkitTask();
        } else {
            return Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * Runs a task synchronously for a specific entity with a delay
     */
    public BukkitTask runTaskLaterForEntity(Entity entity, Runnable task, long delay) {
        if (isFolia) {
            // Folia's EntityScheduler.runDelayed() uses ticks, not milliseconds!
            entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), null, delay);
            return new DummyBukkitTask();
        } else {
            return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * Runs a task synchronously for a specific location
     */
    public BukkitTask runTaskForLocation(Location location, Runnable task) {
        if (isFolia) {
            Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
            return new DummyBukkitTask();
        } else {
            return Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * Runs a task synchronously for a specific location with a delay
     */
    public BukkitTask runTaskLaterForLocation(Location location, Runnable task, long delay) {
        if (isFolia) {
            // Folia's RegionScheduler.runDelayed() uses ticks, not milliseconds!
            Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> task.run(), delay);
            return new DummyBukkitTask();
        } else {
            return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * Runs a task asynchronously
     */
    public BukkitTask runTaskAsynchronously(Runnable task) {
        if (isFolia) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
            return new DummyBukkitTask();
        } else {
            return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
    
    /**
     * Runs a repeating task
     */
    public BukkitTask runTaskTimer(Runnable task, long delay, long period) {
        if (isFolia) {
            // Folia's GlobalRegionScheduler.runAtFixedRate() uses ticks, not milliseconds!
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period);
            return new DummyBukkitTask();
        } else {
            return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
    }
    
    /**
     * Runs a repeating task for a specific entity
     */
    public BukkitTask runTaskTimerForEntity(Entity entity, Runnable task, long delay, long period) {
        if (isFolia) {
            // Folia's EntityScheduler.runAtFixedRate() uses ticks, not milliseconds!
            // No conversion needed - pass ticks directly
            entity.getScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), null, delay, period);
            return new DummyBukkitTask();
        } else {
            return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
    }
    
    /**
     * Checks if the server is running Folia
     */
    public boolean isFolia() {
        return isFolia;
    }
    
    /**
     * Simple BukkitTask implementation for Folia compatibility
     * Since Folia doesn't return BukkitTask objects, we provide a dummy implementation
     */
    private static class DummyBukkitTask implements BukkitTask {
        private boolean cancelled = false;
        
        @Override
        public int getTaskId() {
            return -1; // Folia doesn't use task IDs
        }
        
        @Override
        public Plugin getOwner() {
            return null;
        }
        
        @Override
        public boolean isSync() {
            return false;
        }
        
        @Override
        public boolean isCancelled() {
            return cancelled;
        }
        
        @Override
        public void cancel() {
            cancelled = true;
            // Note: We can't actually cancel Folia tasks once submitted
            // This is a limitation of the abstraction layer
        }
    }
} 