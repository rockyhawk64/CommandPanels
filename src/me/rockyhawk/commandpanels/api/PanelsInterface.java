package me.rockyhawk.commandpanels.api;

import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;

public class PanelsInterface {

    public String playerName;
    private Panel top,middle,bottom = null;

    public PanelsInterface(String player){
        playerName = player;
    }

    //if all panels are closed
    public boolean allClosed(){
        return top == null && middle == null && bottom == null;
    }

    //get the panels based on position
    public void setPanel(Panel panel, PanelPosition position){
        switch(position){
            case Top:{
                if(panel == null && top != null){
                    top.isOpen = false;
                }
                top = panel;
                return;
            }
            case Middle:{
                if(panel == null && middle != null){
                    middle.isOpen = false;
                }
                middle = panel;
                return;
            }
            case Bottom:{
                if(panel == null && bottom != null){
                    bottom.isOpen = false;
                }
                bottom = panel;
            }
        }
    }

    //get the panels based on position
    public Panel getPanel(PanelPosition position){
        switch(position){
            case Top:{
                return top;
            }
            case Middle:{
                return middle;
            }
            case Bottom:{
                return bottom;
            }
        }
        return null;
    }
}
