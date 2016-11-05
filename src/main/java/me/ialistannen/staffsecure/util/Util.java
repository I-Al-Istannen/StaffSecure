package me.ialistannen.staffsecure.util;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import me.ialistannen.staffsecure.StaffSecure;

/**
 * Some static Utility methods
 */
public class Util {

    private static final String METADATA_LOGIN_STATUS_KEY   = "StaffSecure login status";
    private static final String METADATA_LOGIN_ATTEMPTS_KEY = "StaffSecure login attempts";

    /**
     * Translates a message
     *
     * @param key The key to use
     * @param formattingObjects The formatting objects
     *
     * @return The translated String
     */
    public static String tr(String key, Object... formattingObjects) {
        return StaffSecure.getInstance().getLanguage().tr(key, formattingObjects);
    }

    /**
     * Translates a message
     *
     * @param key The key to use
     * @param formattingObjects The formatting objects
     *
     * @return The translated String, with the prefix appended at the front
     */
    public static String trWithPrefix(String key, Object... formattingObjects) {
        return tr("prefix") + tr(key, formattingObjects);
    }

    /**
     * Checks if a player is logged in
     *
     * @param player The player to check
     *
     * @return True if the player is logged in
     */
    public static boolean isLoggedIn(Player player) {
        if (!player.hasMetadata(METADATA_LOGIN_STATUS_KEY)) {
            return false;
        }
        List<MetadataValue> metadata = player.getMetadata(METADATA_LOGIN_STATUS_KEY);

        return !metadata.isEmpty() && metadata.get(0).asBoolean();
    }

    /**
     * Sets the players login status
     *
     * @param player The Player to set it for
     * @param loggedIn Whether the player is logged in
     */
    public static void setLoginStatus(Player player, boolean loggedIn) {
        if (!loggedIn) {
            player.removeMetadata(METADATA_LOGIN_STATUS_KEY, StaffSecure.getInstance());
            return;
        }

        player.setMetadata(METADATA_LOGIN_STATUS_KEY, new FixedMetadataValue(StaffSecure.getInstance(), true));
    }

    /**
     * Gets the failed login attempts of a player
     *
     * @param player The player to check
     *
     * @return The failed attempts of the player
     */
    public static int getFailedLoginAttempts(Player player) {
        if (!player.hasMetadata(METADATA_LOGIN_ATTEMPTS_KEY)) {
            return 0;
        }
        List<MetadataValue> metadata = player.getMetadata(METADATA_LOGIN_ATTEMPTS_KEY);

        if (metadata.isEmpty()) {
            return 0;
        }
        return metadata.get(0).asInt();
    }

    /**
     * Sets the failed login attempts of a player
     *
     * @param player The Player to set it for
     * @param failedAttempts The amount of failed attempts
     */
    public static void setFailedLoginAttempts(Player player, int failedAttempts) {
        if (failedAttempts < 1) {
            player.removeMetadata(METADATA_LOGIN_ATTEMPTS_KEY, StaffSecure.getInstance());
            return;
        }

        player.setMetadata(METADATA_LOGIN_ATTEMPTS_KEY, new FixedMetadataValue(StaffSecure.getInstance(), failedAttempts));
    }

    /**
     * Increases the amount of failed login attempts by one
     *
     * @param player The Player to increase it for
     */
    public static void increaseFailedLoginAttempts(Player player) {
        setFailedLoginAttempts(player, getFailedLoginAttempts(player) + 1);
    }
}
