package uk.co.streefland.rhys.finalyearproject.unused;

import java.io.*;
import java.security.*;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author JavaDigest
 *
 */
public class EncryptionUtil {

    /**
     * String to hold the name of the private key file.
     */
    public static final String PRIVATE_KEY_FILE = "private.smk";

    /**
     * String to hold name of the public key file.
     */
    public static final String PUBLIC_KEY_FILE = "public.smk";

    /**
     * Generate key which contains a pair of private and public key using 1024
     * bytes. Store the set of keys in Private.key and Public.key files.
     *
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void generateKeyPair() {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            final KeyPair key = keyGen.generateKeyPair();

            File privateKeyFile = new File(PRIVATE_KEY_FILE);
            File publicKeyFile = new File(PUBLIC_KEY_FILE);

            // Create files to store public and private key
            if (privateKeyFile.getParentFile() != null) {
                privateKeyFile.getParentFile().mkdirs();
            }
            privateKeyFile.createNewFile();

            if (publicKeyFile.getParentFile() != null) {
                    publicKeyFile.getParentFile().mkdirs();
            }
            publicKeyFile.createNewFile();

            // Saving the Public key in a file
            ObjectOutputStream publicKeyOS = new ObjectOutputStream(
                    new FileOutputStream(publicKeyFile));
            publicKeyOS.writeObject(key.getPublic());
            publicKeyOS.close();

            // Saving the Private key in a file
            ObjectOutputStream privateKeyOS = new ObjectOutputStream(
                    new FileOutputStream(privateKeyFile));
            privateKeyOS.writeObject(key.getPrivate());
            privateKeyOS.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * The method checks if the pair of public and private key has been generated.
     *
     * @return flag indicating if the pair of keys were generated.
     */
    public static boolean areKeysPresent() {

        File privateKey = new File(PRIVATE_KEY_FILE);
        File publicKey = new File(PUBLIC_KEY_FILE);

        if (privateKey.exists() && publicKey.exists()) {
            return true;
        }
        return false;
    }

    public static SecretKey generateSessionKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // for example
        return keyGen.generateKey();
    }

    public static byte[] encryptString(String text, SecretKey sessionKey) {
        byte[] cipherText = null;
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            System.out.println("BLOCK SIZE: " + cipher.getBlockSize());
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, sessionKey, ivSpec);
            cipherText = cipher.doFinal(text.getBytes(("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    public static String decryptString(byte[] text, SecretKey sessionKey) throws UnsupportedEncodingException {
        byte[] decryptedText = null;
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);

            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");


            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, sessionKey, ivSpec);
            decryptedText = cipher.doFinal(text);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new String(decryptedText, "UTF-8");
    }

    /**
     * Encrypt the plain text using public key.
     *
     *          : original plain text
     * @param key
     *          :The public key
     * @return Encrypted text
     * @throws java.lang.Exception
     */
    public static byte[] encryptKey(SecretKey sessionKey, PublicKey key) {
        byte[] cipherText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA");
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(sessionKey.getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    /**
     * Decrypt text using private key.
     *
     *          :encrypted text
     * @param key
     *          :The private key
     * @return plain text
     * @throws java.lang.Exception
     */
    public static SecretKey decryptKey(byte[] sessionKey, PrivateKey key) {
        byte[] decryptedText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA");

            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedText = cipher.doFinal(sessionKey);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new SecretKeySpec(decryptedText, 0, 16, "AES");
    }

    /**
     * Test the EncryptionUtil
     */
    public static void main(String[] args) {

        try {

            // Check if the pair of keys are present else generate those.
            if (!areKeysPresent()) {
                // Method generates a pair of keys using the RSA algorithm and stores it
                // in their respective files
                generateKeyPair();
            }

            SecretKey sessionKey = generateSessionKey();
            System.out.println(sessionKey.getEncoded().length);

            final String originalText = "Text to be encrypted ";
            ObjectInputStream inputStream;

            byte[] cipherText = encryptString(originalText, sessionKey);



            inputStream = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE));
            final PublicKey publicKey = (PublicKey) inputStream.readObject();
            final byte[] encryptedSessionKey = encryptKey(sessionKey, publicKey);

            // Decrypt the cipher text using the private key.
            inputStream = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE));
            final PrivateKey privateKey = (PrivateKey) inputStream.readObject();

            final SecretKey sessionKey2 = decryptKey(encryptedSessionKey, privateKey);


            final String decryptedString = decryptString(cipherText, sessionKey2);

            // Printing the Original, Encrypted and Decrypted Text
            System.out.println("Original: " + originalText);
            System.out.println("Encrypted: " + cipherText.toString());
            System.out.println("Decrypted: " + decryptedString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}