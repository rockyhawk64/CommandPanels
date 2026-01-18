package me.rockyhawk.commandpanels.api;

import org.jspecify.annotations.NonNull;

public interface Registrable {

    default @NonNull String getName() {
        return this.getClass().getName();
    }

}
