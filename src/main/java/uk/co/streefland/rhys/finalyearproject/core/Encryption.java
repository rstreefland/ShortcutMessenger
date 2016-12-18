package uk.co.streefland.rhys.finalyearproject.core;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;

/**
 * Handles encryption and decryption of Strings using the symmetrical AES algorithm
 * Uses the users' password hash as the key
 */
public class Encryption {

    Cipher cipher;

    public Encryption() throws NoSuchPaddingException, NoSuchAlgorithmException {
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    /**
     * Randomly generates an IV (Initialisation Vector)
     *
     * @return
     */
    public byte[] generateIV() {
        byte[] iv = new byte[cipher.getBlockSize()];
        new SecureRandom().nextBytes(iv);
        return iv;
    }


    /**
     * Encrypts a given string with the password hash of the target user
     *
     * @param target  The target user object - needed for the password hash
     * @param iv      The initialisation vector in a byte array
     * @param message The string to encrypt
     * @return The encrypted string as a byte array
     */
    public byte[] encryptString(User target, byte[] iv, String message) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, UnsupportedEncodingException {

        /* Create key and cipher using the password hash of the user */
        Key aesKey = new SecretKeySpec(target.getPasswordHash(), "AES");

        /*  Generate the IVParameterSpec based on the iv byte array */
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        /* Encrypt the string using the key and the iv spec */
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] encrypted = cipher.doFinal(message.getBytes(("UTF-8")));

        return encrypted;
    }

    /**
     * Decrypts an encrypted message into a string given the plaintext password of the target user
     *
     * @param target            The target user object
     * @param plainTextPassword The plaintext password of the local user
     * @param iv                The initialisation vector
     * @param encryptedMessage  The encrypted messgage in byte array
     * @return The decrypted string
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws UnsupportedEncodingException
     */
    public String decryptString(User target, String plainTextPassword, byte[] iv, byte[] encryptedMessage) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {

        /* Hash the local users plain text password to check it matches the existing password hash */
        if (target.doPasswordsMatch(plainTextPassword)) {

            /* Create key and cipher using the password hash of the user */
            Key aesKey = new SecretKeySpec(target.getPasswordHash(), "AES");

            /* Generate the IVParameterSpec based on the iv byte array */
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            /* Decrypt the string using the key and the iv spec */
            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
            byte[] decrypted = cipher.doFinal(encryptedMessage);

            return new String(decrypted, "UTF-8");
        }
        return null;
    }
}
