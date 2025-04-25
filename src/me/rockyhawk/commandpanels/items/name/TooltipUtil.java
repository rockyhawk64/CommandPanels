package me.rockyhawk.commandpanels.items.name;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;

public class TooltipUtil {

    private final Context ctx;

    public TooltipUtil(Context ctx) {
        this.ctx = ctx;
    }

    public void hideTooltip(ItemMeta meta) {
        if (!ctx.version.isAtLeast("1.21.4")) return;

        try {
            Method method = ItemMeta.class.getMethod("setHideTooltip", boolean.class);
            method.invoke(meta, true);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
