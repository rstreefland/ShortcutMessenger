package uk.co.streefland.rhys.finalyearproject.message.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Encryption;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * A simple text message - slowly getting more functional :)
 */
public class TextMessage implements Message, Serializable {

    private static final long serialVersionUID = 1L;

    public static final byte CODE = 0x08;
    private KeyId networkId;
    private Node origin;
    private Node source;
    private Node target;
    private User authorUser;
    private User recipientUser;
    private KeyId messageId;
    private String message;
    private byte[][] encryptedData;
    private long createdTime;

    /**
     * Constructor for user to user messages
     */
    public TextMessage(LocalNode localNode, KeyId messageId, Node target, User recipientUser, String message, long createdTime) {
        this.networkId = localNode.getNetworkId();
        this.origin = localNode.getNode();
        this.source = origin;
        this.target = target;
        this.authorUser = localNode.getUsers().getLocalUser();
        this.recipientUser = recipientUser;
        this.messageId = messageId;
        this.createdTime = createdTime;

        try {
            this.encryptedData = localNode.getEncryption().encrypt(message,recipientUser.getPublicKey());
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | UnsupportedEncodingException | InvalidAlgorithmParameterException | InvalidKeySpecException e) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("Failed to encrypt message with error", e);
        }
    }

    public TextMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        networkId = new KeyId(in);
        origin = new Node(in);
        source = new Node(in);

        /* Only read if target node isn't null */
        if (in.readBoolean()) {
            target = new Node(in);
        }

        /* Only read if origin user isn't null */
        if (in.readBoolean()) {
            authorUser = new User(in);
        }

        /* Only read if target user isn't null */
        if (in.readBoolean()) {
            recipientUser = new User(in);
        }

        createdTime = in.readLong();

        if (in.readBoolean()) {
            /* Read in the encrypted session key */
            int encryptedSessionKeyLength = in.readInt();
            byte[] encryptedSessionKey = new byte[encryptedSessionKeyLength];
            in.readFully(encryptedSessionKey);

            /* Read in the ciphertext */
            int cipherTextLength = in.readInt();
            byte[] cipherText = new byte[cipherTextLength];
            in.readFully(cipherText);

            encryptedData = new byte[][]{encryptedSessionKey, cipherText};
        } else {
            message = in.readUTF();
        }

        messageId = new KeyId(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        networkId.toStream(out);
        origin.toStream(out);
        source.toStream(out);

        if (target != null) {
            out.writeBoolean(true);
            target.toStream(out);
        } else {
            out.writeBoolean(false);
        }

        if (authorUser != null) {
            out.writeBoolean(true);
            authorUser.toStream(out);
        } else {
            out.writeBoolean(false);
        }

        if (recipientUser != null) {
            out.writeBoolean(true);
            recipientUser.toStream(out);
        } else {
            out.writeBoolean(false);
        }

        out.writeLong(createdTime);

        /* Write the initialisation vector and encrypted message to the stream */
        if (encryptedData != null) {
            out.writeBoolean(true);

            out.writeInt(encryptedData[0].length);
            out.write(encryptedData[0]);
            out.writeInt(encryptedData[1].length);
            out.write(encryptedData[1]);
        } else {
            out.writeBoolean(false);
            out.writeUTF(message);
        }

        messageId.toStream(out);
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public KeyId getNetworkId() {
        return networkId;
    }

    @Override
    public Node getOrigin() {
        return origin;
    }

    @Override
    public Node getSource() {
        return source;
    }

    public void setSource(Node source) {
        this.source = source;
    }

    public User getAuthorUser() {
        return authorUser;
    }

    public User getRecipientUser() {
        return recipientUser;
    }

    public KeyId getMessageId() {
        return messageId;
    }

    public String getMessage() {
        return message;
    }

    public byte[][] getEncryptedData() {
        return encryptedData;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    @Override
    public String toString() {
        return "TextMessage[origin KeyId=" + origin.getNodeId() + "]";
    }
}
