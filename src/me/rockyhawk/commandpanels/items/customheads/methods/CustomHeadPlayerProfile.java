package me.rockyhawk.commandpanels.items.customheads.methods;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.rockyhawk.commandpanels.items.customheads.CustomHeadProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class CustomHeadPlayerProfile implements CustomHeadProvider {
    //cached itemstacks stored for access
    public HashMap<String, ItemStack> savedCustomHeads = new HashMap<>();

    //Using the PlayerProfile API for getting custom heads
    public ItemStack getCustomHead(String base64Texture) {
        //check for any saved heads
        if(savedCustomHeads.containsKey(base64Texture)) {
            return savedCustomHeads.get(base64Texture);
        }

        //clear cached textures list until length limit is reached
        Iterator<Map.Entry<String, ItemStack>> iterator = savedCustomHeads.entrySet().iterator();
        while (savedCustomHeads.size() > 2000 && iterator.hasNext()) {
            iterator.next(); // Move to next entry
            iterator.remove(); // Remove the entry
        }

        // Create a new player head ItemStack
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        // Create a new PlayerProfile
        UUID uuid = UUID.randomUUID();  // Unique ID for the profile
        PlayerProfile profile = Bukkit.createPlayerProfile(uuid);

        // Decode the base64 texture and extract the texture URL
        String decodedTexture = extractSkinUrlFromBase64(base64Texture);

        // Set the skin URL using PlayerTextures
        PlayerTextures textures = profile.getTextures();
        try {
            // Using a URL object for the texture
            textures.setSkin(new URL(decodedTexture));
        } catch (MalformedURLException ignore) {} // Base64 has no URL, ignore

        // Apply the textures to the profile
        profile.setTextures(textures);

        // Apply the PlayerProfile to the SkullMeta
        skullMeta.setOwnerProfile(profile);

        // Set the modified SkullMeta back to the ItemStack
        skull.setItemMeta(skullMeta);

        savedCustomHeads.put(base64Texture, skull);
        return skull;
    }

    // New method to get a player head by player name
    public ItemStack getPlayerHead(String playerName) {
        //check for any saved heads
        if(savedCustomHeads.containsKey(playerName)) {
            return savedCustomHeads.get(playerName);
        }

        //clear cached textures list until length limit is reached
        Iterator<Map.Entry<String, ItemStack>> iterator = savedCustomHeads.entrySet().iterator();
        while (savedCustomHeads.size() > 2000 && iterator.hasNext()) {
            iterator.next(); // Move to next entry
            iterator.remove(); // Remove the entry
        }

        // Create a new player head ItemStack
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        // Get the OfflinePlayer object for the provided player name
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

        // Create a PlayerProfile from the player's UUID
        UUID playerUUID = offlinePlayer.getUniqueId();
        PlayerProfile profile = Bukkit.createPlayerProfile(playerUUID);

        // Apply the PlayerProfile to the SkullMeta
        skullMeta.setOwnerProfile(profile);

        // Set the modified SkullMeta back to the ItemStack
        skull.setItemMeta(skullMeta);

        savedCustomHeads.put(playerName, skull);
        return skull;
    }

    // Helper method to extract the skin URL from the base64 texture
    private String extractSkinUrlFromBase64(String base64Texture) {
        // Decode the base64 string
        byte[] decodedBytes = Base64.getDecoder().decode(base64Texture);
        String decodedString = new String(decodedBytes);

        // Parse the decoded string as JSON
        JsonObject jsonObject = JsonParser.parseString(decodedString).getAsJsonObject();

        // Navigate to "textures" -> "SKIN" -> "url"
        JsonObject textures = jsonObject.getAsJsonObject("textures");
        JsonObject skin = textures.getAsJsonObject("SKIN");

        // Return the URL if it exists
        return skin.has("url") ? skin.get("url").getAsString() : null;
    }

}
