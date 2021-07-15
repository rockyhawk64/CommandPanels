package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.api.Panel;

import java.util.HashMap;

public class HotbarPlayerManager {
    public HashMap<Integer,Panel> list = new HashMap<>();

    public HotbarPlayerManager(){
    }

    public void addSlot(int slot, Panel panel){
        list.put(slot,panel);
    }

    public Panel getPanel(int slot){
        return list.get(slot).copy();
    }
}
