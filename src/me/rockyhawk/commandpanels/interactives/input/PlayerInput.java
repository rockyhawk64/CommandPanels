package me.rockyhawk.commandpanels.interactives.input;

import me.rockyhawk.commandpanels.api.Panel;

import java.util.List;

public class PlayerInput {
    public Panel panel;
    public List<String> commands;

    public PlayerInput(Panel panel1, List<String> commands1){
        panel = panel1;
        commands = commands1;
    }
}
