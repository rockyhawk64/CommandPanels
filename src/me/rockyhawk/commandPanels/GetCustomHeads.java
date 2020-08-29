package me.rockyhawk.commandPanels;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class GetCustomHeads {
    commandpanels plugin;
    public GetCustomHeads(commandpanels pl) {
        this.plugin = pl;
    }

    //getting the head from a Player
    @SuppressWarnings("deprecation")
    public ItemStack getPlayerHead(String name) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwner(name);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public ItemStack getCustomHead(String b64stringtexture) {
        //get head from base64
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        PropertyMap propertyMap = profile.getProperties();
        if (propertyMap == null) {
            throw new IllegalStateException("Profile doesn't contain a property map");
        } else {
            propertyMap.put("textures", new Property("textures", b64stringtexture));
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            ItemMeta headMeta = head.getItemMeta();
            assert headMeta != null;
            Class headMetaClass = headMeta.getClass();

            try {
                getField(headMetaClass, "profile", GameProfile.class, 0).set(headMeta, profile);
            } catch (IllegalArgumentException | IllegalAccessException var10) {
                plugin.debug(var10);
            }

            head.setItemMeta(headMeta);
            return head;
        }
    }

    //used with getItem for heads
    private <T> Field getField(Class<?> target, String name, Class<T> fieldType, int index) {
        Field[] var4 = target.getDeclaredFields();

        for (Field field : var4) {
            if ((name == null || field.getName().equals(name)) && fieldType.isAssignableFrom(field.getType()) && index-- <= 0) {
                field.setAccessible(true);
                return field;
            }
        }

        if (target.getSuperclass() != null) {
            return getField(target.getSuperclass(), name, fieldType, index);
        } else {
            throw new IllegalArgumentException("Cannot find field with type " + fieldType);
        }
    }
}
