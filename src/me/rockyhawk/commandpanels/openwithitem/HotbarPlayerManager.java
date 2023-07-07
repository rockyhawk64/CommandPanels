package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.api.Panel;

import java.util.HashMap;

public class HotbarPlayerManager {
    public HashMap<String,Panel> list = new HashMap<>();

    public HotbarPlayerManager(){
    }

    public void addSlot(String slot, Panel panel){
        list.put(slot,panel);
    }

    public Panel getPanel(String slot){
        return list.get(slot).copy();
    }
}
