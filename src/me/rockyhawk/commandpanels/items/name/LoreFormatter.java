package me.rockyhawk.commandpanels.items.name;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoreFormatter {

    private final Context ctx;

    public LoreFormatter(Context ctx) {
        this.ctx = ctx;
    }

    public List<String> format(Panel panel, List<String> lore, Player player) {
        return ctx.text.placeholdersList(panel, PanelPosition.Top, player, lore);
    }

    public List<String> splitListWithEscape(List<String> list) {
        List<String> output = new ArrayList<>();
        for (String str : list) {
            output.addAll(Arrays.asList(str.split("\\\\n")));
        }
        return output;
    }
}