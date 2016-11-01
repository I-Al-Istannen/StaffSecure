package me.ialistannen.staffsecure.hashing;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hashes a User password
 */
public enum PasswordHashGenerator {

    INSTANCE;

    private final SecureRandom RANDOM = new SecureRandom();

/*
    private final int ITERATION_COUNT = StaffSecure.getInstance() == null ? 2000 : StaffSecure.getInstance().getConfig().getInt("iteration.count");
    private final int SALT_LENGTH     = StaffSecure.getInstance() == null ? 256 : StaffSecure.getInstance().getConfig().getInt("salt.length");
    private final int KEY_LENGTH      = StaffSecure.getInstance() == null ? 512 : StaffSecure.getInstance().getConfig().getInt("key.length");
*/
    
    /**
     * Checks if a password is correct
     * <p>
     * Side-effect: Overwrites password array to destroy password
     *
     * @param expectedHash The expected Hash
     * @param salt The salt
     * @param password the password the user entered
     * @param iterationCount The amount of iterations
     * @param keyLength The length of the key
     *
     * @return True if the password is correct
     */
    public boolean isExpectedPassword(byte[] expectedHash, byte[] salt, char[] password, int iterationCount, int keyLength) {
        byte[] newHash = hash(salt, password, iterationCount, keyLength);

        Arrays.fill(password, Character.MIN_VALUE);

        return Arrays.equals(expectedHash, newHash);
    }

    /**
     * Hashes the password with the salt
     * <p>
     * Side-effect: Overwrites password array to destroy password
     *
     * @param salt The salt of the password
     * @param password The password
     * @param iterationCount The iteration count
     * @param keyLength The length of the key
     *
     * @return The hashed password
     */
    public byte[] hash(byte[] salt, char[] password, int iterationCount, int keyLength) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        // bye, bye array
        Arrays.fill(password, Character.MIN_VALUE);

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Falling back to Sha1");
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                return factory.generateSecret(spec).getEncoded();
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e1) {
                throw new RuntimeException("algorithm invalid", e);
            }
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Returns a new random salt
     *
     * @param length The length of the salt
     *
     * @return The salt
     */
    public byte[] getSalt(int length) {
        byte[] array = new byte[length];
        RANDOM.nextBytes(array);

        return array;
    }
}
