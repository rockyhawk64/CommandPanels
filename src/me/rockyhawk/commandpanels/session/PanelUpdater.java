package me.rockyhawk.commandpanels.session;

import me.rockyhawk.commandpanels.Context;

public interface PanelUpdater {
    void start(Context ctx, PanelSession session);
    void stop();
}

