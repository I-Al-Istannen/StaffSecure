package me.ialistannen.staffsecure.playerdata;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import com.perceivedev.perceivecore.time.DurationParser;

import me.ialistannen.staffsecure.StaffSecure;

/**
 * Manages the tokens for a player
 */
public class PlayerTokenManager {

    private final SecureRandom   SECURE_RANDOM   = new SecureRandom();
    private final Base64.Encoder BASE_64_ENCODER = Base64.getEncoder();

    private final Duration tokenLifeSpan;
    private final Map<UUID, Token> tokenMap = new HashMap<>();
    private BukkitRunnable cleaner;

    /**
     * @param tokenLifeSpan The lifespan of tokens
     */
    private PlayerTokenManager(Duration tokenLifeSpan) {
        this.tokenLifeSpan = tokenLifeSpan;
    }

    /**
     * Reads the token lifespan from the config
     *
     * @see #PlayerTokenManager(Duration)
     */
    public PlayerTokenManager() {
        this(Duration.ofMillis(DurationParser.parseDuration(StaffSecure.getInstance().getConfig().getString("token.lifespan"))));
    }

    /**
     * Adds a token for a player
     *
     * @param uuid The {@link UUID} to check
     * @param token The token
     * @param expireTime The time it expires
     */
    private void addToken(UUID uuid, String token, LocalDateTime expireTime) {
        tokenMap.put(uuid, new Token(token, expireTime));

        if (cleaner == null) {
            startCleaner();
        }
    }

    /**
     * Generates a token and adds that. Then returns it
     * <p>
     * Uses a secure random encoded in Base64.
     *
     * @param uuid The {@link UUID} of the player
     *
     * @return The generated token
     */
    public String generateAndAddToken(UUID uuid) {
        byte[] tokenBits = new byte[StaffSecure.getInstance().getConfig().getInt("token.bytes.amount")];
        SECURE_RANDOM.nextBytes(tokenBits);
        String tokenString = BASE_64_ENCODER.encodeToString(tokenBits);

        addToken(uuid, tokenString, LocalDateTime.now().plus(tokenLifeSpan));
        return tokenString;
    }

    /**
     * Removes the token for the player
     *
     * @param uuid The {@link UUID} of the player
     */
    public void removeToken(UUID uuid) {
        tokenMap.remove(uuid);

        if (tokenMap.isEmpty()) {
            stopCleaner();
        }
    }

    /**
     * Checks a token for a player
     *
     * @param uuid The {@link UUID} to check
     * @param token The token to verify
     *
     * @return True if the token is correct
     */
    public boolean isCorrectToken(UUID uuid, String token) {
        return contains(uuid) && tokenMap.get(uuid).isCorrectToken(token);
    }

    /**
     * Checks if an ID is in this manager
     *
     * @param uuid The {@link UUID} to check
     *
     * @return True if the {@link UUID} is in this manager
     */
    public boolean contains(UUID uuid) {
        return tokenMap.containsKey(uuid);
    }

    /**
     * Starts the cleaner
     */
    private void startCleaner() {
        cleaner = new BukkitRunnable() {
            @Override
            public void run() {
                cleanExpired(LocalDateTime.now());
            }
        };
        cleaner.runTaskTimer(StaffSecure.getInstance(), 0, 20);
    }

    /**
     * Stops the cleaner
     */
    private void stopCleaner() {
        if (cleaner != null) {
            cleaner.cancel();
        }
        cleaner = null;
    }

    /**
     * Cleans expired tokens
     *
     * @param now The current time
     */
    private void cleanExpired(LocalDateTime now) {
        tokenMap.values().removeIf(token -> token.isExpired(now));
    }

    /**
     * A token
     */
    private static class Token {
        private final String        token;
        private final LocalDateTime expireTime;

        /**
         * @param token The token
         * @param expireTime the time it expires
         */
        private Token(String token, LocalDateTime expireTime) {
            this.token = token;
            this.expireTime = expireTime;
        }

        /**
         * Checks if the two tokens are equal
         *
         * @param other The other token
         *
         * @return True if the two tokens are equal
         */
        private boolean isCorrectToken(String other) {
            return token.equals(other);
        }

        /**
         * Checks if the token is expired
         *
         * @param time The current time
         *
         * @return True if the token is expired
         */
        private boolean isExpired(LocalDateTime time) {
            return time.isAfter(expireTime);
        }
    }
}
