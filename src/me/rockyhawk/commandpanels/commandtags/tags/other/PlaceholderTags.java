package me.rockyhawk.commandpanels.commandtags.tags.other;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.CommandTagEvent;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlaceholderTags implements Listener {
    CommandPanels plugin;
    public PlaceholderTags(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("placeholder=")) {
            e.commandTagUsed();
            //placeholder is placeholder= [%placeholder%:value]
            String cmd;
            cmd = String.join(" ",e.raw);

            if(e.panel == null){
                return;
            }

            Character[] cm = ArrayUtils.toObject(cmd.toCharArray());
            for(int i = 0; i < cm.length; i++){
                if(cm[i].equals('[')){
                    String contents = cmd.substring(i+1, i+cmd.substring(i).indexOf(']'));
                    //do not change the placeholder
                    String placeholder = contents.substring(0,contents.indexOf(':'));
                    //only convert placeholders for the value
                    String value = plugin.tex.placeholders(e.panel,e.pos,e.p,contents.substring(contents.indexOf(':')+1));
                    e.panel.placeholders.addPlaceholder(placeholder,value);
                    i = i+contents.length()-1;
                }
            }
            return;
        }
        if(e.name.equalsIgnoreCase("add-placeholder=")) {
            e.commandTagUsed();
            //this will only run the placeholder command if the placeholder doesn't yet exist in the panel
            String cmd;
            cmd = String.join(" ",e.raw);

            if (e.panel == null) {
                return;
            }

            Character[] cm = ArrayUtils.toObject(cmd.toCharArray());
            for (int i = 0; i < cm.length; i++) {
                if (cm[i].equals('[')) {
                    String contents = cmd.substring(i + 1, i + cmd.substring(i).indexOf(']'));
                    //do not change the placeholder
                    String placeholder = contents.substring(0, contents.indexOf(':'));
                    //only convert placeholders for the value
                    if (!e.panel.placeholders.keys.containsKey(placeholder)) {
                        //only convert placeholders for the value
                        String value = plugin.tex.placeholders(e.panel,e.pos, e.p, contents.substring(contents.indexOf(':') + 1));
                        e.panel.placeholders.addPlaceholder(placeholder, value);
                    }
                    i = i + contents.length() - 1;
                }
            }
        }
    }
}
