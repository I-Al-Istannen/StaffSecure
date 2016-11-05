package me.ialistannen.staffsecure.playerdata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.ialistannen.staffsecure.StaffSecure;
import me.ialistannen.staffsecure.hashing.PasswordHashGenerator;

/**
 * Manages the Player passwords
 */
public class PlayerDataManager {

    private final Path saveDir;
    private final Map<UUID, PlayerData> cache = new LinkedHashMap<UUID, PlayerData>(20) {
        private final int MAX_ENTRIES = 20;

        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, PlayerData> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    /**
     * @param saveDir The save directory
     */
    public PlayerDataManager(Path saveDir) {
        this.saveDir = saveDir;
    }

    /**
     * Adds a player and saves him to the file
     *
     * @param uuid The {@link UUID} of the Player
     * @param password The password of the Player
     */
    public void addPlayer(UUID uuid, String password) {
        int saltLength = StaffSecure.getInstance().getConfig().getInt("salt.length");
        int keyLength = StaffSecure.getInstance().getConfig().getInt("key.length");
        int iterationCount = StaffSecure.getInstance().getConfig().getInt("iteration.count");

        byte[] newSalt = PasswordHashGenerator.INSTANCE.getSalt(saltLength);

        byte[] newPassword = PasswordHashGenerator.INSTANCE.hash(newSalt, password.toCharArray(), iterationCount, keyLength);

        byte[] inetAddress = Bukkit.getPlayer(uuid) == null ? null : Bukkit.getPlayer(uuid).getAddress().getAddress().getAddress();

        PlayerData data = new PlayerData(newPassword, newSalt, iterationCount, keyLength, saltLength, inetAddress);

        saveToFile(data, uuid);
        
        cache.put(uuid, data);
    }

    /**
     * Validates a password
     *
     * @param uuid The {@link UUID} of the Player
     * @param password The password they entered
     *
     * @return True if the password is correct
     */
    public boolean isCorrect(UUID uuid, String password) {
        if (!isRegistered(uuid)) {
            return false;
        }

        PlayerData data = getPlayerData(uuid);
        if (data == null) {
            return false;
        }

        if (!PasswordHashGenerator.INSTANCE.isExpectedPassword(data.getHashedPassword(), data.getSalt(), password.toCharArray(),
                  data.getIterationCount(), data.getKeyLength())) {
            return false;
        }

        // save with new config settings, if needed
        if (StaffSecure.getInstance().getConfig().getInt("iteration.count") != data.getIterationCount()
                  || StaffSecure.getInstance().getConfig().getInt("key.length") != data.getKeyLength()
                  || StaffSecure.getInstance().getConfig().getInt("salt.length") != data.getSaltLength()) {

            addPlayer(uuid, password);
        }

        return true;
    }

    /**
     * Checks if a player is registered
     *
     * @param uuid The {@link UUID} of the Player
     *
     * @return True if the player is registered
     */
    public boolean isRegistered(UUID uuid) {
        return getConfig(uuid) != null;
    }

    /**
     * Loads the {@link PlayerData}
     * <p>
     * If needed, it reads from the file system
     *
     * @param uuid The {@link UUID} of the Player
     *
     * @return The {@link PlayerData} for him
     */
    public PlayerData getPlayerData(UUID uuid) {
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }
        return loadFromFile(uuid);
    }

    /**
     * Saves the {@link PlayerData}
     *
     * @param data The {@link PlayerData} to save
     * @param uuid The {@link UUID} of the Player
     */
    public void saveToFile(PlayerData data, UUID uuid) {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("data", data);

        File saveFile = saveDir.resolve(uuid.toString()).resolve("data.yml").toFile();

        try {
            configuration.save(saveFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the PlayerData from file
     *
     * @param uuid The {@link UUID} of the Player
     *
     * @return The PlayerData, or null if none found
     */
    private PlayerData loadFromFile(UUID uuid) {
        FileConfiguration configuration = getConfig(uuid);

        if (configuration == null) {
            StaffSecure.getInstance().getLogger().warning("Error reading player file, UUID={" + uuid + "}");
            return null;
        }

        PlayerData data = (PlayerData) configuration.get("data");
        if (data != null) {
            cache.put(uuid, data);
        }

        return data;
    }

    /**
     * Returns the file configuration
     *
     * @param uuid The {@link UUID} of the Player
     *
     * @return The {@link FileConfiguration} or null if an error occurred
     */
    private FileConfiguration getConfig(UUID uuid) {
        File playerDataFile = saveDir.resolve(uuid.toString()).resolve("data.yml").toFile();
        if (!playerDataFile.exists()) {
            StaffSecure.getInstance().getLogger().fine("Error reading player file, UUID='" + uuid + "', error='File not found'");
            return null;
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(playerDataFile);

        if (!configuration.contains("data") || !(configuration.get("data") instanceof PlayerData)) {
            StaffSecure.getInstance().getLogger().fine("Error reading player file, UUID='" + uuid + "', error='Data path not found, file corrupted?'");
            return null;
        }
        return configuration;
    }
}
