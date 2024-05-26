package me.rockyhawk.commandpanels.classresources.customheads;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
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

public class GetCustomHeads {
    CommandPanels plugin;
    public GetCustomHeads(CommandPanels pl) {
        this.plugin = pl;
    }

    //will not go above 2000 elements in the list
    public HashSet<SavedCustomHead> savedCustomHeads = new HashSet<>();

    public String getHeadBase64(ItemStack head) {
        if (plugin.getHeads.ifSkullOrHead(head.getType().toString()) && head.hasItemMeta()) {
            //check if the head is a HeadDatabase head first
            if(plugin.getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
                HeadDatabaseAPI api = new HeadDatabaseAPI();
                try {
                    String base64 = api.getBase64(head);
                    if(base64 != null){
                        return base64;
                    }
                } catch (Exception ignore) {}
            }
            //try getting Base64 of a custom head
            if (!(head.getItemMeta() instanceof SkullMeta)) return null;
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta == null) return null;

            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                GameProfile profile = (GameProfile) profileField.get(meta);

                Collection<Property> textures = profile.getProperties().get("textures");
                if (!textures.isEmpty()) {
                    // Directly accessing fields within the Property object via reflection.
                    Property textureProperty = textures.iterator().next();
                    Field valueField = textureProperty.getClass().getDeclaredField("value");
                    valueField.setAccessible(true);
                    return (String) valueField.get(textureProperty);
                }

            } catch (Exception error) {
                plugin.debug(error, null);
            }
        }
        return null;
    }

    //getting the head from a Player Name
    public ItemStack getPlayerHead(String name) {
        byte id = 0;
        if (plugin.legacy.MAJOR_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)) {
            id = 3;
        }

        //get texture if already cached
        for(SavedCustomHead head : savedCustomHeads){
            if(head.playerName == null) {continue;}
            if(head.playerName.equals(name)){
                return head.headItem;
            }
        }

        //create ItemStack
        ItemStack itemStack = new ItemStack(Material.matchMaterial(plugin.getHeads.playerHeadString()), 1, id);

        //Run fallback code, if API call fails, use legacy setOwner
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwner(name);
        itemStack.setItemMeta(meta);

        // Fetch and cache the texture asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if(plugin.debug.consoleDebug){
                    plugin.getServer().getConsoleSender().sendMessage(plugin.tex.colour(plugin.tag +
                            ChatColor.WHITE +
                            "Attempting to Download & Cache Head Texture for " + name));
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
                Bukkit.getScheduler().runTask(plugin, () -> {
                    itemStack.setItemMeta(getCustomHead(value).getItemMeta());
                    savedCustomHeads.add(new SavedCustomHead(itemStack, value, name));
                });
            } catch (Exception ignore) {
                // Ignore as errors should be skipped and no need to show in console
            }
        });

        return itemStack;
    }

    //used to get heads from Base64 Textures
    @SuppressWarnings("deprecation")
    public ItemStack getCustomHead(String b64stringtexture) {
        //check for any saved heads
        for(SavedCustomHead head : savedCustomHeads){
            if(head.base64.equals(b64stringtexture)){
                return head.headItem;
            }
        }

        //get head from base64
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        PropertyMap propertyMap = profile.getProperties();
        if (propertyMap == null) {
            throw new IllegalStateException("Profile doesn't contain a property map");
        } else {
            propertyMap.put("textures", new Property("textures", b64stringtexture));
            byte id = 0;
            if(plugin.legacy.MAJOR_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)){
                id = 3;
            }
            ItemStack head = new ItemStack(Material.matchMaterial(plugin.getHeads.playerHeadString()), 1,id);
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
                plugin.debug(e1,null);
            }

            head.setItemMeta(headMeta);
            return head;
        }
    }
}