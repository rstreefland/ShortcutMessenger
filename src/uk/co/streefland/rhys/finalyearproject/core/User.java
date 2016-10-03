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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Rhys on 07/09/2016.
 */
public class User implements Serializable{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

        this.associatedNodes = new ArrayList<>();
    }

    public User(DataInputStream in) throws IOException
    {
        fromStream(in);
    }

    public void toStream(DataOutputStream out) throws IOException
    {
         /* Add the KeyId to the stream */
        userId.toStream(out);

        out.writeUTF(userName);

        out.write(passwordHash);

        out.write(passwordSalt);

        out.writeInt(associatedNodes.size());

        for (Node node : associatedNodes) {
            node.toStream(out);
        }
    }

    public final void fromStream(DataInputStream in) throws IOException
    {
        /* Read the userId */
        userId = new KeyId(in);

        userName = in.readUTF();

        passwordHash = new byte[16];
        in.readFully(passwordHash);

        passwordSalt = new byte[16];
        in.readFully(passwordSalt);

        int associatedNodesSize = in.readInt();

        associatedNodes = new ArrayList<>();

        for (int i = 0; i < associatedNodesSize; i++) {
            associatedNodes.add(new Node(in));
        }
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
        logger.debug("Hashing password took: " + difference + "ms");
        return passwordHash;
    }

    public boolean doPasswordsMatch(String password) {
        if (Arrays.equals(generatePasswordHash(passwordSalt, password), passwordHash)) {
            return true;
        } else {
            return false;
        }
    }

    public KeyId getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }
}
