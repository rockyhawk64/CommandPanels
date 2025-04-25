package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class TitleTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.startsWith("title=")) return false;

        String[] args = command.split("\\s");
        args = Arrays.copyOfRange(args, 1, args.length); // Remove the tag name

        if (args.length >= 5) {
            Player p = Bukkit.getPlayer(args[0]);
            StringBuilder message = new StringBuilder();
            for (int i = 4; i < args.length; i++) {
                message.append(args[i]).append(" ");
            }
            message.deleteCharAt(message.length() - 1);

            String title;
            String subtitle = "";
            if (message.toString().contains("/n/")) {
                title = ctx.text.placeholders(panel, pos, player, message.toString().split("/n/")[0]);
                subtitle = ctx.text.placeholders(panel, pos, player, message.toString().split("/n/")[1]);
            } else {
                title = ctx.text.placeholders(panel, pos, player, message.toString().trim());
            }

            try {
                p.sendTitle(title, subtitle, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            } catch (Exception ex) {
                ctx.debug.send(ex, player, ctx);
            }
            return true;
        }
        return false;
    }
}
