package uk.co.streefland.rhys.finalyearproject.core;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Provides an encryption and decryption mechanism similar to that of PGP. RSA public and private keys are used for asymmetric encryption.
 * AES is used for symmetric encryption.
 */
public class Encryption {

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public Encryption() throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        loadKeys();
    }

    /**
     * Encrypts a String using the public key of the intended recipient
     * @param text The inputs String to encrypt
     * @param recipientPublicKeyBytes The bytes of the recipient's public key
     * @return 2D byte array containing encrypted session key and ciphertext
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeySpecException
     */
    public byte[][] encrypt(String text, byte[] recipientPublicKeyBytes) throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException, InvalidKeySpecException {

        /* Recreate the PublicKey object from the byte array */
        KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
        PublicKey recipientPublicKey = kf.generatePublic(new X509EncodedKeySpec(recipientPublicKeyBytes));

        /* Perform the encryption */
        SecretKey sessionKey = generateSessionKey();
        byte[] encryptedSessionKey = encryptKey(sessionKey, recipientPublicKey);
        byte[] cipherText = encryptString(text, sessionKey);

        /* Return the encrypted session key and ciphertext merged into one 2D byte array */
        byte[][] encryptedData = {encryptedSessionKey, cipherText};
        return encryptedData;
    }

    /**
     * Decrypts the encrypted data produced by encrypt() using the local private key
     * @param encyptedData 2D byte array containing encrypted session key and ciphertext
     * @return Plaintext in String format
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     */
    public String decrypt(byte[][] encyptedData) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {

        /* Split the 2D array into the respective arrays */
        byte[] encryptedSessionKey = encyptedData[0];
        byte[] cipherText = encyptedData[1];

        /* Decrypt the session key and ciphertext */
        SecretKey sessionKey = decryptKey(encryptedSessionKey);
        String text = decryptString(cipherText, sessionKey);

        return text;
    }

    /**
     * Loads an existing public key and private key pair from the respective files.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchAlgorithmException
     */
    private void loadKeys() throws IOException, ClassNotFoundException, NoSuchAlgorithmException {

        if (!areKeysPresent()) {
            return;
        }

        ObjectInputStream ois;

        /* Read the public key */
        ois = new ObjectInputStream(new FileInputStream(Configuration.PUBLIC_KEY_FILE));
        publicKey = (PublicKey) ois.readObject();
        ois.close();

        /* Read the private key */
        ois = new ObjectInputStream(new FileInputStream(Configuration.PRIVATE_KEY_FILE));
        privateKey = (PrivateKey) ois.readObject(); // TODO: 24/01/2017 potential error
        if (ois.readBoolean()) {
            ois.readUTF();
        }
        ois.close();
    }

    /**
     * Generates a pair of RSA keys and stores them in their respective files
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public void generateKeyPair() throws NoSuchAlgorithmException, IOException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(Configuration.RSA_BITS);
        KeyPair key = keyGen.generateKeyPair();

        File privateKeyFile = new File(Configuration.PRIVATE_KEY_FILE);
        File publicKeyFile = new File(Configuration.PUBLIC_KEY_FILE);

        /* Create files to store public and private key */
        privateKeyFile.createNewFile();
        publicKeyFile.createNewFile();

        /* Save the public key to file */
        ObjectOutputStream pkOOS = new ObjectOutputStream(new FileOutputStream(publicKeyFile));
        pkOOS.writeObject(key.getPublic());
        pkOOS.close();

        /* Save the private key to file */
        ObjectOutputStream prkOOS = new ObjectOutputStream(new FileOutputStream(privateKeyFile));
        prkOOS.writeObject(key.getPrivate());
        prkOOS.writeBoolean(false);  // Username is not being written
        prkOOS.close();

        publicKey = key.getPublic();
        privateKey = key.getPrivate();
    }

    /**
     * Stores the username associated with the private key in the private key file
     * @param userName
     * @throws IOException
     */
    public void updateUsername(String userName) throws IOException {

        File privateKeyFile = new File(Configuration.PRIVATE_KEY_FILE);

        ObjectOutputStream prkOOS = new ObjectOutputStream(new FileOutputStream(privateKeyFile));
        prkOOS.writeObject(privateKey);
        prkOOS.writeBoolean(true);
        prkOOS.writeUTF(userName);
        prkOOS.close();
    }

    /**
     * Generates the session key using the AES algorithm
     * @return
     * @throws NoSuchAlgorithmException
     */
    private SecretKey generateSessionKey() throws NoSuchAlgorithmException {

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(Configuration.AES_BITS); // for example
        return keyGen.generateKey();
    }

    /**
     * Encrypts the session key with the public key of the intended recipient
     * @param sessionKey The session key
     * @param publicKey The public key of the intended recipient
     * @return The encrypted session key in byte array format
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private byte[] encryptKey(SecretKey sessionKey, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedSessionKey = cipher.doFinal(sessionKey.getEncoded());

        return encryptedSessionKey;
    }

    /**
     * Decrypts the session key using the local private key
     * @param encryptedSessionKey The encrypted session key to be decrypted
     * @return The session key
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private SecretKey decryptKey(byte[] encryptedSessionKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] sessionKey = cipher.doFinal(encryptedSessionKey);

        return new SecretKeySpec(sessionKey, 0, 16, "AES");
    }

    /**
     * Encrypts the plaintext with the session key and the AES algorithm
     * @param text The plaintext to encrypt
     * @param sessionKey The session key used for encryption
     * @return The encrypted ciphertext in byte array format
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private byte[] encryptString(String text, SecretKey sessionKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {

        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey, ivSpec);

        byte[] cipherText = cipher.doFinal(text.getBytes(("UTF-8")));
        return cipherText;
    }

    /**
     * Decrypts the ciphertext using the session key
     * @param cipherText The ciphertext in byte array format
     * @param sessionKey The session key
     * @return The decrypted plaintext in String format
     * @throws UnsupportedEncodingException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private String decryptString(byte[] cipherText, SecretKey sessionKey) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, ivSpec);

        byte[] decryptedText = cipher.doFinal(cipherText);
        return new String(decryptedText, "UTF-8");
    }

    /**
     * Tests a provided public key against the local private key to determine if the keys are part of the same pair
     * @param publicKeyBytes The public key in byte format
     * @return True if both keys are part of the same pair, false if not
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public boolean testKeypair(byte[] publicKeyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
        return rsaPublicKey.getModulus().equals(rsaPrivateKey.getModulus())
                && BigInteger.valueOf(2).modPow(rsaPublicKey.getPublicExponent()
                        .multiply(rsaPrivateKey.getPrivateExponent()).subtract(BigInteger.ONE),
                rsaPublicKey.getModulus()).equals(BigInteger.ONE);
    }

    /**
     * Tests if the key files exist
     * @return True if both files exist, false if not
     */
    public boolean areKeysPresent() {

        File privateKeyFile = new File(Configuration.PRIVATE_KEY_FILE);
        File publicKeyFile = new File(Configuration.PUBLIC_KEY_FILE);

        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            return true;
        }
        return false;
    }

    /**
     * Deletes the key files
     */
    public void deleteKeys() {

        File privateKeyFile = new File(Configuration.PRIVATE_KEY_FILE);
        File publicKeyFile = new File(Configuration.PUBLIC_KEY_FILE);

        privateKeyFile.delete();
        publicKeyFile.delete();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Extracts the username from the private key file
     * @return The username
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static String getUserName() throws IOException, ClassNotFoundException {

        File privateKeyFile = new File(Configuration.PRIVATE_KEY_FILE);
        File publicKeyFile = new File(Configuration.PUBLIC_KEY_FILE);
        String userName = null;

        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            ObjectInputStream prkOIS = new ObjectInputStream(new FileInputStream(Configuration.PRIVATE_KEY_FILE));
            prkOIS.readObject();
            if (prkOIS.readBoolean()) {
                userName = prkOIS.readUTF();
            }
            prkOIS.close();
        }

        return userName;
    }
}