package me.rockyhawk.commandpanels.session.inventory.backend;

public enum InventoryCloseReason {
    CLIENT_CLOSE(true),
    REPLACED(true),
    QUIT(true),
    DEATH(true),
    TELEPORT(true),
    DISABLE(true),
    INVALID_CLICK(true),
    CONDITION_FAILED(true),
    REFRESH(false);

    private final boolean runCloseCommands;

    InventoryCloseReason(boolean runCloseCommands) {
        this.runCloseCommands = runCloseCommands;
    }

    public boolean shouldRunCloseCommands() {
        return runCloseCommands;
    }
}
