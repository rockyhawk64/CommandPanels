package me.rockyhawk.commandpanels.items.customheads.methods;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.items.customheads.CustomHeadProvider;
import me.rockyhawk.commandpanels.items.customheads.SavedCustomHead;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CustomHeadGameProfile implements CustomHeadProvider {
    Context ctx;
    public CustomHeadGameProfile(Context pl) {
        this.ctx = pl;
    }

    public HashMap<String, SavedCustomHead> savedCustomHeads = new HashMap<>();

    //getting the head from a Player Name
    public ItemStack getPlayerHead(String name) {
        byte id = 0;
        if(ctx.version.isBelow("1.13")){
            id = 3;
        }

        //get texture if already cached
        if (savedCustomHeads.containsKey(name)) {
            if (!savedCustomHeads.get(name).isValid && (System.currentTimeMillis() - savedCustomHeads.get(name).lastAttempt) < 60000) {
                // If the last attempt was less than 60 seconds ago and was invalid, return null or a default item
                return new ItemStack(Material.valueOf(ctx.version.isBelow("1.13") ? "SKULL_ITEM" : "PLAYER_HEAD"));
            }
            if(savedCustomHeads.get(name).isValid) {
                return savedCustomHeads.get(name).headItem; // Return cached item if valid
            }
        }

        //create ItemStack
        ItemStack itemStack = new ItemStack(Material.matchMaterial(ctx.version.isBelow("1.13") ? "SKULL_ITEM" : "PLAYER_HEAD"), 1, id);

        //Run fallback code, if API call fails, use legacy setOwner
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwner(name);
        itemStack.setItemMeta(meta);

        // Fetch and cache the texture asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(ctx.plugin, () -> {
            try {
                if(ctx.debug.consoleDebug){
                    Bukkit.getServer().getConsoleSender().sendMessage(ctx.text.colour(ctx.tag +
                            ChatColor.WHITE +
                            "Download & Cache Head Texture for " + name));
                }

                // Fetch the player UUID from the Mojang API
                URL uuidUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
                URLConnection uuidConnection = uuidUrl.openConnection();
                uuidConnection.setConnectTimeout(2000); // Set connection timeout to 2 seconds
                uuidConnection.setReadTimeout(2000); // Set read timeout to 2 seconds
                //Json is simple and structured so a hard code solution will avoid the need for a library
                String uuidReader = new Scanner(uuidConnection.getInputStream(),
                        StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
                String uuid = uuidReader.split("\"id\" : \"")[1].split("\"")[0];

                // Fetch the skin texture from the Mojang API using the player UUID
                URL texturesUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
                URLConnection texturesConnection = texturesUrl.openConnection();
                texturesConnection.setConnectTimeout(2000); // Set connection timeout to 2 seconds
                texturesConnection.setReadTimeout(2000); // Set read timeout to 2 seconds
                //Json is simple and structured so a hard code solution will avoid the need for a library
                String valueReader = new Scanner(texturesConnection.getInputStream(),
                        StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
                String value = valueReader.split("\"value\" : \"")[1].split("\"")[0];

                // Once the API call is finished, update the ItemStack on the main thread
                Bukkit.getScheduler().runTask(ctx.plugin, () -> {
                    itemStack.setItemMeta(getCustomHead(name, value).getItemMeta());
                    savedCustomHeads.put(name, new SavedCustomHead(itemStack, value, true));
                });
            } catch (Exception ignore) {
                Bukkit.getScheduler().runTask(ctx.plugin, () -> {
                    //do not overwrite a valid cached head
                    if(savedCustomHeads.containsKey(name) && savedCustomHeads.get(name).isValid){
                        return;
                    }
                    savedCustomHeads.put(name, new SavedCustomHead(null, null, false)); // Mark as invalid
                });
            }
        });

        return itemStack;
    }

    //will also use cached heads feature to get heads if player name is provided
    public ItemStack getCustomHead(String playerName, String b64stringtexture) {
        //check for any saved heads
        if(savedCustomHeads.containsKey(playerName)) {
            if (savedCustomHeads.get(playerName).base64 != null) {
                return savedCustomHeads.get(playerName).headItem;
            }
            savedCustomHeads.get(playerName).isValid = false;
        }

        //clear cached textures list until length limit is reached
        Iterator<Map.Entry<String, SavedCustomHead>> iterator = savedCustomHeads.entrySet().iterator();
        while (savedCustomHeads.size() > 2000 && iterator.hasNext()) {
            iterator.next(); // Move to next entry
            iterator.remove(); // Remove the entry
        }

        //if saved head is not found from player name, get head manually
        return getCustomHead(b64stringtexture);
    }

    //used to get heads from Base64 Textures
    @SuppressWarnings("deprecation")
    public ItemStack getCustomHead(String b64stringtexture) {
        //get head from base64
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        PropertyMap propertyMap = profile.getProperties();
        if (propertyMap == null) {
            throw new IllegalStateException("Profile doesn't contain a property map");
        } else {
            propertyMap.put("textures", new Property("textures", b64stringtexture));
            byte id = 0;
            if(ctx.version.isBelow("1.13")){
                id = 3;
            }
            ItemStack head = new ItemStack(Material.matchMaterial(ctx.version.isBelow("1.13") ? "SKULL_ITEM" : "PLAYER_HEAD"), 1,id);
            ItemMeta headMeta = head.getItemMeta();
            assert headMeta != null;

            Field profileField;
            Method setProfileMethod = null;
            try {
                // Attempt to access the 'profile' field directly
                // Also writes to 'serializedProfile' field as one cannot be null while the other is not
                // This block is mainly for 1.20.2+ versions
                profileField = headMeta.getClass().getDeclaredField("profile");
                Field serializedProfileField = headMeta.getClass().getDeclaredField("serializedProfile");

                profileField.setAccessible(true);
                serializedProfileField.setAccessible(true);

                profileField.set(headMeta, profile);
                serializedProfileField.set(headMeta, profile); // Assuming serializedProfile is of the same type
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
                try {
                    // This block covers versions that have a 'setProfile' method instead of direct field access
                    // Likely for versions prior to 1.20.2
                    setProfileMethod = headMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                } catch (NoSuchMethodException ignore) {}
            } catch (SecurityException ignored) {}
            try {
                if (setProfileMethod == null) {
                    // Attempt to access the 'profile' field directly
                    // This block is a generic fallback for versions lacking the 'setProfile' method
                    profileField = headMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(headMeta, profile);
                } else {
                    // Use the 'setProfile' method if it was found
                    setProfileMethod.setAccessible(true);
                    setProfileMethod.invoke(headMeta, profile);
                }
            } catch (Exception e1) {
                ctx.debug.send(e1,null, ctx);
            }

            head.setItemMeta(headMeta);
            return head;
        }
    }
}