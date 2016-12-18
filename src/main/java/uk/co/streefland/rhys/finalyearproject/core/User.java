package uk.co.streefland.rhys.finalyearproject.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

/**
 * Represents a User on the network
 */
public class User implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private KeyId userId;
    private String userName;
    private byte[] passwordHash;
    private byte[] passwordSalt;
    private Node associatedNode;
    private long registerTime;
    private long lastActiveTime;

    public User(String userName, String password) {
        this.userId = new KeyId(userName);
        this.userName = userName;
        this.passwordSalt = generateSalt();
        this.passwordHash = generatePasswordHash(this.passwordSalt, password);
    }

    public User(DataInputStream in) throws IOException {
        fromStream(in);
    }

    public void toStream(DataOutputStream out) throws IOException {
        /* Add userId and userName to stream */
        userId.toStream(out);
        out.writeUTF(userName);

        /* Add password hash and salt to stream */
        out.write(passwordHash);
        out.write(passwordSalt);

        /* Write associatedNode to the stream */
        if (associatedNode == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            associatedNode.toStream(out);
        }

        out.writeLong(registerTime);
        out.writeLong(lastActiveTime);
    }

    private void fromStream(DataInputStream in) throws IOException {
        /* Read in userId and userName */
        userId = new KeyId(in);
        userName = in.readUTF();

        /* Read in password hash and salt */
        passwordHash = new byte[16];
        in.readFully(passwordHash);
        passwordSalt = new byte[16];
        in.readFully(passwordSalt);

        /* Read in associatedNode */
        if (in.readBoolean()) {
            associatedNode = new Node(in);
        }

        registerTime = in.readLong();
        lastActiveTime = in.readLong();
    }

    /**
     * Generates a 16 byte salt randomly
     *
     * @return byte array containing the salt
     */
    private byte[] generateSalt() {
        final Random r = new SecureRandom();
        byte[] salt = new byte[16];
        r.nextBytes(salt);
        return salt;
    }

    /**
     * Generates a secure 128bit password hash using the provided salt and password.
     * Uses the PBEKeySpec method with 5000 passes
     *
     * @param salt     The salt to hash the password with
     * @param password The plaintext password to hash
     * @return The generated password hash
     */
    private byte[] generatePasswordHash(byte[] salt, String password) {
        long start = System.currentTimeMillis();
        byte[] passwordHash = null;

        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 5000, 128);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            passwordHash = f.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Error while generating password hash", e);
        }
        return passwordHash;
    }

    /**
     * Compares a plaintext password with the hashed one stored in the object
     *
     * @param password The plaintext password to compare
     * @return True if passwords match, false if not
     */
    public boolean doPasswordsMatch(String password) {
        return Arrays.equals(generatePasswordHash(passwordSalt, password), passwordHash);
    }

    /**
     * Adds a node to the associated nodes list of the User
     *
     * @param newNode The node to add to the list of associated nodes
     * @return False if node already exists in the associated nodes list
     */
    public void addAssociatedNode(Node newNode) {
        associatedNode = newNode;
    }

    public KeyId getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime() {
        /* Only allow setting of register time once */
        if (registerTime == 0L) {
            registerTime = new Date().getTime();
            lastActiveTime = registerTime;
        }
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime() {
        lastActiveTime = new Date().getTime();
    }

    public Node getAssociatedNode() {
        return associatedNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(userName);
        return sb.toString();
    }
}
