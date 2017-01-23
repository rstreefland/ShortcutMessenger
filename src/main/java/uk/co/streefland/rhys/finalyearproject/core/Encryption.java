package uk.co.streefland.rhys.finalyearproject.core;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Encryption {

    public static final String PRIVATE_KEY_FILE = "private.smk";
    public static final String PUBLIC_KEY_FILE = "public.smk";

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public Encryption() throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        loadOrCreateKeys();
        System.out.println("BYTES: " + getPublicKey().getEncoded().length);
    }

    public byte[][] encrypt(String text, byte[] recipientPublicKeyBytes) throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
       // PublicKey recipientPublicKey =
        //        KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(recipientPublicKeyBytes));
        System.out.println("BYTES" + recipientPublicKeyBytes.length);

        KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever
        PublicKey recipientPublicKey = kf.generatePublic(new X509EncodedKeySpec(recipientPublicKeyBytes));

        SecretKey sessionKey = generateSessionKey();
        byte[] encryptedSessionKey = encryptKey(sessionKey, recipientPublicKey);
        byte[] cipherText = encryptString(text, sessionKey);

        byte[][] encryptedData = {encryptedSessionKey, cipherText};
        return encryptedData;
    }

    public String decrypt(byte[][] encyptedData) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        byte[] encryptedSessionKey = encyptedData[0];
        byte[] cipherText = encyptedData[1];

        SecretKey sessionKey = decryptKey(encryptedSessionKey);
        String text = decryptString(cipherText, sessionKey);

        return text;
    }

    private void loadOrCreateKeys() throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        if (!areKeysPresent()) {
            generateKeyPair();
        }

        ObjectInputStream inputStream;
        inputStream = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE));
        publicKey = (PublicKey) inputStream.readObject();

        inputStream.close();

        inputStream = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE));
        privateKey = (PrivateKey) inputStream.readObject();

        inputStream.close();
    }

    private SecretKey generateSessionKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // for example
        return keyGen.generateKey();
    }

    private boolean areKeysPresent() {
        File privateKey = new File(PRIVATE_KEY_FILE);
        File publicKey = new File(PUBLIC_KEY_FILE);

        if (privateKey.exists() && publicKey.exists()) {
            return true;
        }
        return false;
    }

    private void generateKeyPair() throws NoSuchAlgorithmException, IOException {
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
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
    }

    private byte[] encryptKey(SecretKey sessionKey, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        // get an RSA cipher object and print the provider
        final Cipher cipher = Cipher.getInstance("RSA");
        // encrypt the plain text using the public key
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherText = cipher.doFinal(sessionKey.getEncoded());
        return cipherText;
    }

    private SecretKey decryptKey(byte[] sessionKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        // get an RSA cipher object and print the provider
        final Cipher cipher = Cipher.getInstance("RSA");

        // decrypt the text using the private key
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedText = cipher.doFinal(sessionKey);

        return new SecretKeySpec(decryptedText, 0, 16, "AES");
    }

    private byte[] encryptString(String text, SecretKey sessionKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
        // get an RSA cipher object and print the provider
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        System.out.println("BLOCK SIZE: " + cipher.getBlockSize());
        // encrypt the plain text using the public key
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey, ivSpec);

        byte[] cipherText = cipher.doFinal(text.getBytes(("UTF-8")));
        return cipherText;
    }

    private String decryptString(byte[] cipherText, SecretKey sessionKey) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);

        // get an RSA cipher object and print the provider
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // decrypt the text using the private key
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, ivSpec);
        byte[] decryptedText = cipher.doFinal(cipherText);

        return new String(decryptedText, "UTF-8");
    }

    public boolean doPublicKeysMatch(byte[] publicKey) {
        if (this.publicKey.getEncoded() == publicKey) {
            return true;
        }
        return false;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}