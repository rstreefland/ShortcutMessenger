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
import java.security.PublicKey;
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

    private static final long serialVersionUID = 1L;

    private KeyId userId;
    private String userName;
    private byte[] publicKey;
    private Node associatedNode;
    private long registerTime;
    private long lastActiveTime;

    public User(String userName) {
        this.userId = new KeyId(userName);
        this.userName = userName;
    }

    public User(String userName, byte[] publicKey) {
        this.userId = new KeyId(userName);
        this.userName = userName;
        this.publicKey = publicKey;
    }

    public User(DataInputStream in) throws IOException {
        fromStream(in);
    }

    public void toStream(DataOutputStream out) throws IOException {
        /* Add userId and userName to stream */
        userId.toStream(out);
        out.writeUTF(userName);

        /* Add password hash and salt to stream */
        if (publicKey == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeInt(publicKey.length);
            out.write(publicKey);
        }

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
        if (in.readBoolean()) {
            int publicKeyLength = in.readInt();

            publicKey = new byte[publicKeyLength];
            in.readFully(publicKey);
        }

        /* Read in associatedNode */
        if (in.readBoolean()) {
            associatedNode = new Node(in);
        }

        registerTime = in.readLong();
        lastActiveTime = in.readLong();
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

    public byte[] getPublicKey() {
        return publicKey;
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

    public void setAssociatedNode(Node associatedNode) {
        this.associatedNode = associatedNode;
    }

    @Override
    public String toString() {
        return userName;
    }
}
