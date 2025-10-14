package me.rockyhawk.commandpanels.builder.inventory.items.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.rockyhawk.commandpanels.Context;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CustomHeads {

    private final Context ctx;
    private static final int MAX_CACHE_SIZE = 5000;
    private static final Map<String, PlayerProfile> profileCache =
            // Used to define a set limit for how long profileCache can get
            Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, PlayerProfile> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            });
    private static final Queue<String> lookupQueue = new ConcurrentLinkedQueue<>();
    private static boolean queueTaskRunning = false;

    /**
     * This class must have a single instance across the plugin
     * so that heads are cached and the queue is utilised properly.s
     */
    public CustomHeads(Context ctx) {
        this.ctx = ctx;
    }

    // ===========================
    // BASE64 (SYNC ONLY)
    // ===========================

    public ItemStack getCustomHead(String base64Texture) {
        PlayerProfile profile = getOrCreateProfile(base64Texture);
        if (profile == null) return new ItemStack(Material.PLAYER_HEAD);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        if (!(skull.getItemMeta() instanceof SkullMeta skullMeta)) return skull;

        skullMeta.setPlayerProfile(profile);
        skull.setItemMeta(skullMeta);

        return skull; // New item each time, only shares profile (skin)
    }

    private PlayerProfile getOrCreateProfile(String base64Texture) {
        return profileCache.computeIfAbsent(base64Texture, key -> {
            String skinUrl = extractSkinUrlFromBase64(base64Texture);
            if (skinUrl == null) return null;

            try {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                PlayerTextures textures = profile.getTextures();
                textures.setSkin(new URL(skinUrl));
                profile.setTextures(textures);
                return profile;
            } catch (MalformedURLException e) {
                return null;
            }
        });
    }

    private String extractSkinUrlFromBase64(String base64Texture) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Texture);
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(decodedString).getAsJsonObject();
            JsonObject textures = jsonObject.getAsJsonObject("textures");
            JsonObject skin = textures.getAsJsonObject("SKIN");
            return skin.has("url") ? skin.get("url").getAsString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ===========================
    // PLAYER HEADS (SYNC + ASYNC)
    // ===========================

    /**
     * If head is not cached run the async warmer to cache the head async.
     */
    public ItemStack getPlayerHeadSync(String playerName) {
        String key = playerName.toLowerCase();

        // Start with a PLAYER_HEAD item and a skull meta
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        // Try to get a profile from cache first
        PlayerProfile profile = profileCache.get(key);

        if (profile == null) {
            // Fallback to offline player profile to show textures when players are already online
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            profile = offlinePlayer.getPlayerProfile();

            // Enqueue async task to fill cache for next time
            enqueuePlayerHead(key);
        }

        // Put profile on the head
        skullMeta.setPlayerProfile(profile);
        skull.setItemMeta(skullMeta);

        return skull;
    }


    /**
     * Asynchronous cache warmer.
     * Will resolve and store the PlayerProfile in the cache in the background.
     * Does not return an ItemStack.
     */
    private void cachePlayerHeadAsync(String playerName) {
        String key = playerName.toLowerCase();
        if (profileCache.containsKey(key)) return; // already cached

        Bukkit.getAsyncScheduler().runNow(ctx.plugin, (t) -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            PlayerProfile p = offlinePlayer.getPlayerProfile();
            if (p == null) {
                UUID offlineUuid = UUID.nameUUIDFromBytes(playerName.getBytes(StandardCharsets.UTF_8));
                p = Bukkit.createProfile(offlineUuid, playerName);
            }
            p.complete(true); // network call
            profileCache.put(key, p);
        });
    }

    /**
     * Player head async queue code below
     * queue is used to help avoid timeouts from Mojang
     */
    private void enqueuePlayerHead(String key) {
        if (profileCache.containsKey(key) || lookupQueue.contains(key)) return;
        lookupQueue.add(key);
        startQueueProcessor();
    }
    private void startQueueProcessor() {
        if (queueTaskRunning) return;
        queueTaskRunning = true;

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(ctx.plugin, task -> {
            int maxPerTick = 3;
            for (int i = 0; i < maxPerTick; i++) {
                String next = lookupQueue.poll();
                if (next == null) {
                    // no more tasks, stop processor
                    task.cancel();
                    queueTaskRunning = false;
                    return;
                }
                // Run the async fetch for this key
                Bukkit.getAsyncScheduler().runNow(ctx.plugin, t -> cachePlayerHeadAsync(next));
            }
        }, 1, 15); // tick interval per head api lookup
    }
}
