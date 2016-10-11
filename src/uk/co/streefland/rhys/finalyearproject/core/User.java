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
import java.util.*;

/**
 * Represents a User on the network
 */
public class User implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private KeyId userId;
    private String userName;
    private byte[] passwordHash;
    private byte[] passwordSalt;
    private List<Node> associatedNodes;
    private long registerTime;
    private long lastLoginTime;

    public User(String userName, String password) {
        this.userId = new KeyId(userName);
        this.userName = userName;

        this.passwordSalt = generateSalt();
        this.passwordHash = generatePasswordHash(this.passwordSalt, password);

        this.associatedNodes = new ArrayList<>();
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

        /* Add all associated nodes to the stream
         * And the quantity so we know how many to read back in */
        out.writeInt(associatedNodes.size());
        for (Node node : associatedNodes) {
            node.toStream(out);
        }

        out.writeLong(registerTime);
        out.writeLong(lastLoginTime);
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

        /* Read in all associated nodes */
        int associatedNodesSize = in.readInt();
        associatedNodes = new ArrayList<>();
        for (int i = 0; i < associatedNodesSize; i++) {
            associatedNodes.add(new Node(in));
        }

        registerTime = in.readLong();
        lastLoginTime = in.readLong();
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

        /* Performance testing */
        long end = System.currentTimeMillis();
        long difference = end - start;
        logger.debug("Hashing password took: " + difference + "ms");

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
    public boolean addAssociatedNode(Node newNode) {
        for (Node node : associatedNodes) {
            if (node.getNodeId().equals(newNode.getNodeId())) {
                return false;
            }
        }

        associatedNodes.add(newNode);
        return true;
    }

    public KeyId getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime() {
        /* Only allow setting of register time once */
        if (registerTime == 0L) {
            registerTime = new Date().getTime();
            lastLoginTime = registerTime; // set the login time as well
        }
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime() {
        this.lastLoginTime = new Date().getTime();
    }

    public List<Node> getAssociatedNodes() {
        return associatedNodes;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("User: " + userName);
        sb.append("\nAssociated nodes: \n");
        for (Node node : associatedNodes) {
            sb.append("Node: ");
            sb.append(node.getSocketAddress().getHostName());
            sb.append("\n");
        }
        return sb.toString();
    }
}
