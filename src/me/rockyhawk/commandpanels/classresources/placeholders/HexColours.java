package me.rockyhawk.commandpanels.classresources.placeholders;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexColours {
    CommandPanels plugin;
    public HexColours(CommandPanels pl) {
        this.plugin = pl;
    }

    public String translateHexColorCodes(String message) {
        //add all the different HEX combinations, in order to ensure they do not cancel each other out
        message = doTranslation(message,formatRegex("hexcodes.start_tag"),formatRegex("hexcodes.end_tag"));
        return message;
    }

    //used to translate hex colours into ChatColors
    private String doTranslation(String message, String startTag, String endTag) {
        final Pattern hexPattern = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }

    //automatically format regex to escape special characters
    public String formatRegex(String path){
        String inputString = plugin.config.getString(path);
        final String[] metaCharacters = {"\\","^","$","{","}","[","]","(",")",".","*","+","?","|","<",">","-","&","%"};

        for (String metaCharacter : metaCharacters) {
            assert inputString != null;
            if (inputString.contains(metaCharacter)) {
                inputString = inputString.replace(metaCharacter, "\\" + metaCharacter);
            }
        }
        return inputString;
    }
}
