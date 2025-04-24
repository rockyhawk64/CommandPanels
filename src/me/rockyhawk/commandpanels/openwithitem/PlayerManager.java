package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.api.Panel;

import java.util.HashMap;

public class PlayerManager {
    public HashMap<String,Panel> list = new HashMap<>();

    // Object for Hotbar items to store where stationary items are
    public PlayerManager(){
    }

    public void addSlot(String slot, Panel panel){
        list.put(slot,panel);
    }

    public Panel getPanel(String slot){
        return list.get(slot).copy();
    }
}
