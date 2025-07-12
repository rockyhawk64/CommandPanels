package me.rockyhawk.commandpanels.builder.inventory.items.utils;

import me.rockyhawk.commandpanels.Context;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LoreFormatter {

    private final Context ctx;

    public LoreFormatter(Context ctx) {
        this.ctx = ctx;
    }

    public List<Component> format(List<String> lore, Player player) {
        List<Component> output = new ArrayList<>();
        for(String line : lore){
            output.add(ctx.text.parseTextToComponent(player, line));
        }
        return output;
    }
}