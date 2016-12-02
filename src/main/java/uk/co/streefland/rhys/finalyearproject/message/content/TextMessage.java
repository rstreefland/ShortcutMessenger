package uk.co.streefland.rhys.finalyearproject.message.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Encryption;
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
import java.util.Date;

/**
 * A simple text message - slowly getting more functional :)
 */
public class TextMessage implements Message, Serializable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final byte CODE = 0x08;
    private KeyId networkId;
    private Node origin;
    private Node source;
    private Node target;
    private User authorUser;
    private User recipientUser;
    private KeyId messageId;
    private String message;
    private byte[] encryptedMessage;
    private byte[] iv;
    private long createdTime;

    /**
     * Constructor for a node sending a message to itself
     */
    public TextMessage(KeyId networkId, KeyId messageId, Node origin, User authorUser, String message, long createdTime) {
        this.networkId = networkId;
        this.origin = origin;
        this.source = origin;
        this.target = origin;
        this.authorUser = authorUser;
        this.recipientUser = authorUser;
        this.messageId = messageId;
        this.createdTime = createdTime;

        this.message = message;
    }

    /**
     * Constructor for user to user messages
     */
    public TextMessage(KeyId networkId, KeyId messageId, Node origin, Node target, User authorUser, User recipientUser, String message, long createdTime) {
        this.networkId = networkId;
        this.origin = origin;
        this.source = origin;
        this.target = target;
        this.authorUser = authorUser;
        this.recipientUser = recipientUser;
        this.messageId = messageId;
        this.createdTime = createdTime;

        try {
            Encryption enc = new Encryption();
            iv = enc.generateIV();
            encryptedMessage = enc.encryptString(recipientUser, iv, message);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
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
            /* Read in the initialisation vector */
            iv = new byte[16];
            in.readFully(iv);

            /* Read in the encrypted message */
            int encryptedMessageLength = in.readInt();
            encryptedMessage = new byte[encryptedMessageLength];
            in.readFully(encryptedMessage);
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
        if (encryptedMessage != null) {
            out.writeBoolean(true);
            out.write(iv);
            out.writeInt(encryptedMessage.length);
            out.write(encryptedMessage);
        } else {
            out.writeBoolean(false);
            out.writeUTF(message);
        }

        messageId.toStream(out);
    }

    @Override
    public String toString() {
        return "TextMessage[origin KeyId=" + origin.getNodeId() + "]";
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

    public Node getTarget() {
        return target;
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

    public byte[] getIv() {
        return iv;
    }

    public byte[] getEncryptedMessage() {
        return encryptedMessage;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCreatedTime() {
        return createdTime;
    }
}
