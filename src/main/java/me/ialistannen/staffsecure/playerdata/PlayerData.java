package me.ialistannen.staffsecure.playerdata;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * Contains Data about the player
 */
public class PlayerData implements ConfigurationSerializable {

    private static final Base64.Decoder DECODER = Base64.getDecoder();
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    private final byte[]      hashedPassword;
    private final byte[]      salt;
    private final int         iterationCount;
    private final int         keyLength;
    private final int         saltLength;
    private       InetAddress lastAddress;

    /**
     * Used by {@link ConfigurationSerializable}
     */
    public PlayerData(Map<String, Object> map) {
        this(DECODER.decode((String) map.get("hash")),
                  DECODER.decode((String) map.get("salt")),
                  ((Number) map.get("iterationCount")).intValue(),
                  ((Number) map.get("keyLength")).intValue(),
                  ((Number) map.get("saltLength")).intValue(),
                  map.get("lastAddress") == null ? null : DECODER.decode((String) map.get("lastAddress")));
    }

    /**
     * @param hashedPassword The hashed password
     * @param salt The hashed salt
     * @param iterationCount The iteration count used
     * @param keyLength The used length of the key
     * @param saltLength The length of the salt
     */
    public PlayerData(byte[] hashedPassword, byte[] salt, int iterationCount, int keyLength, int saltLength, byte[] lastAddress) {
        this.hashedPassword = hashedPassword;
        this.salt = salt;
        this.iterationCount = iterationCount;
        this.keyLength = keyLength;
        this.saltLength = saltLength;
        try {
            this.lastAddress = lastAddress == null ? null : InetAddress.getByAddress(lastAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Inet address unknown", e);
        }
    }

    /**
     * The hash of the password
     *
     * @return The Hashed password
     */
    public byte[] getHashedPassword() {
        return hashedPassword;
    }

    /**
     * Returns the salt
     *
     * @return The salt
     */
    public byte[] getSalt() {
        return salt;
    }

    /**
     * Returns the iteration count
     *
     * @return The IterationCount
     */
    public int getIterationCount() {
        return iterationCount;
    }

    /**
     * Returns the length of the key
     *
     * @return The length of the key
     */
    public int getKeyLength() {
        return keyLength;
    }

    /**
     * Returns the length of the salt
     *
     * @return the length of the salt
     */
    public int getSaltLength() {
        return saltLength;
    }

    /**
     * Returns the last {@link InetAddress} of the player
     *
     * @return The {@link InetAddress} of the player. May be null.
     */
    public InetAddress getLastAddress() {
        return lastAddress;
    }

    /**
     * Returns the last {@link InetAddress} the player had
     *
     * @param lastAddress The last {@link InetAddress} the player had
     */
    public void setLastAddress(InetAddress lastAddress) {
        this.lastAddress = lastAddress;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("hash", ENCODER.encodeToString(hashedPassword));
        map.put("salt", ENCODER.encodeToString(salt));
        map.put("iterationCount", iterationCount);
        map.put("keyLength", keyLength);
        map.put("saltLength", saltLength);
        if (lastAddress != null) {
            map.put("lastAddress", ENCODER.encodeToString(lastAddress.getAddress()));
        }

        return map;
    }
}
