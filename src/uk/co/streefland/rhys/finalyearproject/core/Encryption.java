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
 * Created by Rhys on 12/10/2016.
 */
public class Encryption {

    Cipher cipher;

    public Encryption() throws NoSuchPaddingException, NoSuchAlgorithmException {
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    /**
     * Randomly generates an IV (Initialisation Vector)
     * @return
     */
    public byte[] generateIV() {
        byte[] iv = new byte[cipher.getBlockSize()];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public byte[] encryptString(User target, byte[] iv, String message) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, UnsupportedEncodingException {

        // Create key and cipher
        Key aesKey = new SecretKeySpec(target.getPasswordHash(), "AES");

        // Generate IvParameterSpec based on iv byte array
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Encrypt
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] encrypted = cipher.doFinal(message.getBytes(("UTF-8")));

        return encrypted;
    }

    public String decryptString(User target, User localUser, byte[] iv, byte[] encryptedMessage) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {

        if (target.doPasswordsMatch(localUser.getPlainTextPassword())) {

            // Create key and cipher
            Key aesKey = new SecretKeySpec(target.getPasswordHash(), "AES");

            // Generate IvParameterSpec based on iv byte array
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
            byte[] decrypted = cipher.doFinal(encryptedMessage);

            return new String(decrypted, "UTF-8");
        }
        return null;
    }
}
