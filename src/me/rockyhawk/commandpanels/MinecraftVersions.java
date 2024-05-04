package me.rockyhawk.commandpanels;

import org.bukkit.Bukkit;

import java.util.Arrays;

public class MinecraftVersions {

    /**
     * Splits the MC Version in multiple parts and returns them.
     *
     * @return String[]
     */
    private static String[] getMinecraftVersionParts(){
        String serverVersion = Bukkit.getServer().getVersion();
        String[] versionParts = serverVersion.split(" "); // Splitting the version string by space
        return versionParts[2].split("\\."); // Splitting the Minecraft version by dot
    }

    /**
     * Gets the major MC version from the Server
     * Example: Returns the 20 of 1.20.6.
     *
     * @return String
     */
    private static String getMajorVersion(){
        String[] minecraftVersionParts = getMinecraftVersionParts();
        // Returns the 20 of 1.20.6
        return minecraftVersionParts[1];
    }

    /**
     * Gets the minor MC version from the Server
     * Example: Returns the 6 of 1.20.6.
     *
     * @return String
     */
    private static String getMinorVersion(){
        String[] minecraftVersionParts = getMinecraftVersionParts();

        // Returns the 6 of 1.20.6
        return minecraftVersionParts[2];

    }

    /**
     * Checks if the version of the Server is older or on the specified Version.
     *
     * @param specifiedVersion The version to check against
     * @return String
     */
    public static boolean lessThanOrEqualTo(String specifiedVersion){
        // Split the Version into its parts
        String[] specifiedVersionParts = specifiedVersion.split("\\.");

        // Get the parts from the Version of the Server.
        // The .replace turns 1.20.4) into 1.20.4
        int serverMajorVersion = Integer.parseInt(String.valueOf(getMajorVersion()).replace(")", ""));
        int serverMinorVersion = Integer.parseInt(String.valueOf(getMinorVersion()).replace(")", ""));

        // Get the parts from the specified version
        int specifiedMajorVersion = Integer.parseInt(specifiedVersionParts[1]);
        int specifiedMinorVersion = Integer.parseInt(specifiedVersionParts[2]);

        // If Major version is less or equal than the specified version.
        if(serverMajorVersion < specifiedMajorVersion){
            return true;
        } // If Major version equal and Minor Version is less or equal than specified version.
        else if (serverMajorVersion == specifiedMajorVersion && serverMinorVersion <= specifiedMinorVersion) {
            return true;
        }else{
            return false;
        }
    }

    /**
     * Checks if the version of the Server is newer or on the specified Version.
     *
     * @param specifiedVersion The version to check against
     * @return String
     */
    public static boolean greaterThanOrEqualTo(String specifiedVersion){
        // Split the Version into its parts
        String[] specifiedVersionParts = specifiedVersion.split("\\.");

        // Get the parts from the Version of the Server
        // The .replace turns 1.20.4) into 1.20.4
        int serverMajorVersion = Integer.parseInt(String.valueOf(getMajorVersion()).replace(")", ""));
        int serverMinorVersion = Integer.parseInt(String.valueOf(getMinorVersion()).replace(")", ""));

        // Get the parts from the specified version
        int specifiedMajorVersion = Integer.parseInt(specifiedVersionParts[1]);
        int specifiedMinorVersion = Integer.parseInt(specifiedVersionParts[2]);

        // If Major version is less or equal than the specified version.
        if(serverMajorVersion > specifiedMajorVersion){
            return true;
        } // If Major version equal and Minor Version is less or equal than specified version.
        else if (serverMajorVersion == specifiedMajorVersion && serverMinorVersion >= specifiedMinorVersion) {
            return true;
        }else{
            return false;
        }
    }
}
