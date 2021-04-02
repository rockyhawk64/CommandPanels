package me.rockyhawk.commandpanels.customcommands;

import java.util.HashMap;

public class PanelPlaceholders {
    public HashMap<String,String> keys;

    public void addPlaceholder(String placeholder, String argument){
        keys.put(placeholder,argument);
    }

    public PanelPlaceholders(){
        keys = new HashMap<>();
    }
}
