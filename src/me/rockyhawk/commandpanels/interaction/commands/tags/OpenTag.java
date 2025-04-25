package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class OpenTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.startsWith("open=")) return false;

        String[] args = command.split("\\s");
        String panelName = ctx.text.placeholders(panel, pos, player, args[1]);

        String cmd = command.replace("open=","").replace(panelName,"").trim();

        Panel openPanel = null;
        PanelPosition openPosition = pos;
        for (Panel pane : ctx.plugin.panelList) {
            if (pane.getName().equals(panelName)) {
                openPanel = pane.copy();
            }
        }
        if (openPanel == null) {
            return false;
        }

        Character[] cm = convertToCharacterArray(cmd.toCharArray());
        for (int i = 0; i < cm.length; i++) {
            if (cm[i].equals('[')) {
                String contents = cmd.substring(i + 1, i + cmd.substring(i).indexOf(']'));
                String placeholder = contents.substring(0, contents.indexOf(':'));
                String value = ctx.text.placeholders(panel, pos, player, contents.substring(contents.indexOf(':') + 1));
                openPanel.placeholders.addPlaceholder(placeholder, value);
                i += contents.length() - 1;
            } else if (cm[i].equals('{')) {
                String contents = cmd.substring(i + 1, i + cmd.substring(i).indexOf('}'));
                openPosition = PanelPosition.valueOf(contents);
                i += contents.length() - 1;
            }
        }
        openPanel.open(player, openPosition);
        return true;
    }

    private Character[] convertToCharacterArray(char[] charArray) {
        Character[] characterArray = new Character[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            characterArray[i] = charArray[i];
        }
        return characterArray;
    }
}
