package me.rockyhawk.commandpanels.versions;

import org.bukkit.Bukkit;

public class VersionManager {

    private final int versionCode;  // e.g. 12005 for 1.20.5

    public VersionManager() {
        String version = Bukkit.getServer().getBukkitVersion().split("-")[0];
        this.versionCode = parseVersionCode(version);
    }

    public int parseVersionCode(String version) {
        String[] parts = version.split("\\.");
        int major = parts.length > 0 ? parseIntSafe(parts[0]) : 0;
        int minor = parts.length > 1 ? parseIntSafe(parts[1]) : 0;
        int patch = parts.length > 2 ? parseIntSafe(parts[2]) : 0;
        return major * 1_000_000 + minor * 1_000 + patch; // Limits: Major 2147, Minor 483, Patch 647
    }

    private int parseIntSafe(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isAtLeast(String version) {
        return versionCode >= parseVersionCode(version);
    }

    public boolean isBelow(String version) {
        return versionCode < parseVersionCode(version);
    }

    public int getVersionCode() {
        return versionCode;
    }
}