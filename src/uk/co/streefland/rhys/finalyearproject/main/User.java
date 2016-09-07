package uk.co.streefland.rhys.finalyearproject.main;

import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;

/**
 * Created by Rhys on 07/09/2016.
 */
public class User {

    private KeyId userId;
    private String userName;
    private byte[] passwordHash;
    private byte[] passwordSalt;
    private List<Node> associatedNodes;

    public User(String userName, String password) {
        this.userId = new KeyId(userName);
        this.userName = userName;

        this.passwordSalt = generateSalt();
        this.passwordHash = generatePasswordHash(this.passwordSalt, password);
    }

    private byte[] generateSalt() {
        final Random r = new SecureRandom();
        byte[] salt = new byte[16];
        r.nextBytes(salt);
        return salt;
    }
    
    private byte[] generatePasswordHash(byte[] salt, String password) {
        long start = System.currentTimeMillis();
        byte[] passwordHash = null;

        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 5000, 128);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            passwordHash = f.generateSecret(spec).getEncoded();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        long difference = end-start;
        System.out.println("Hashing password took: " + difference + "ms");
        return passwordHash;
    }

    public boolean doPasswordsMatch(String password) {
        if (Arrays.equals(generatePasswordHash(passwordSalt, password), passwordHash)) {
            return true;
        } else {
            return false;
        }
    }
}
