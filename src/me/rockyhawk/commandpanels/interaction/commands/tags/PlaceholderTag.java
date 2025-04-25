package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import org.bukkit.entity.Player;

public class PlaceholderTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.startsWith("placeholder=")) {
            return false;
        }

        String cmd = command.substring("placeholder=".length()).trim();
        if (panel == null) {
            return true;
        }

        Character[] cm = convertToCharacterArray(cmd.toCharArray());
        for (int i = 0; i < cm.length; i++) {
            if (cm[i].equals('[')) {
                String contents = cmd.substring(i + 1, i + cmd.substring(i).indexOf(']'));
                String placeholder = contents.substring(0, contents.indexOf(':'));
                String value = ctx.text.placeholders(panel, pos, player, contents.substring(contents.indexOf(':') + 1));
                panel.placeholders.addPlaceholder(placeholder, value);
                i = i + contents.length() - 1;
            }
        }
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
