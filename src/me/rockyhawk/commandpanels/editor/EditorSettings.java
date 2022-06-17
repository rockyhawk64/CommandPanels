package me.rockyhawk.commandpanels.editor;

public class EditorSettings {
    public String panelName;
    public String menuOpen;
    public String slotSelected = "0";
    public boolean hasEditorOpen = false;

    public EditorSettings(String menu, String panel){
        menuOpen = menu;
        panelName = panel;
    }

    public void setMenuOpen(String menu){
        menuOpen = menu;
    }

    public void setLastPanel(String panel){
        panelName = panel;
    }
}
