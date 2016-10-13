package uk.co.streefland.rhys.finalyearproject.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Encryption;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * A simple text message - slowly getting more functional :)
 */
public class TextMessage implements Message {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final byte CODE = 0x05;
    private Node origin;
    private Node target;
    private User originUser;
    private User targetUser;
    private KeyId messageId;
    private String message;
    private byte[] encryptedMessage;
    private byte[] iv;
    private long createdTime;

    /**
     * Constructor for broadcast messages
     */
    public TextMessage(Node origin, String message) {
        this.origin = origin;
        this.messageId = new KeyId();
        this.message = message;
        this.createdTime = new Date().getTime() / 1000; // store timestamp in seconds
    }

    /**
     * Constructor for user to user messages
     */
    public TextMessage(Node origin, Node target, User originUser, User targetUser, String message) {
        this.origin = origin;
        this.target = target;
        this.originUser = originUser;
        this.targetUser = targetUser;
        this.messageId = new KeyId();
        this.createdTime = new Date().getTime() / 1000; // store timestamp in seconds

        try {
            Encryption enc = new Encryption();
            iv = enc.generateIV();
            encryptedMessage = enc.encryptString(targetUser, iv, message);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
            logger.error("Failed to encrypt message with error", e);
        }
    }

    public TextMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        origin = new Node(in);

        /* only read if target node isn't null */
        if (in.readBoolean()) {
            target = new Node(in);
        }

        /* only read if origin user isn't null */
        if (in.readBoolean()) {
            originUser = new User(in);
        }

        /* only read if target user isn't null */
        if (in.readBoolean()) {
            targetUser = new User(in);
        }

        createdTime = in.readLong();

         /* Read in encrypted message and iv */
        iv = new byte[16];
        in.readFully(iv);

        int encryptedMessageLength= in.readInt();
        encryptedMessage = new byte[encryptedMessageLength];
        in.readFully(encryptedMessage);

        messageId = new KeyId(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);

        if (target != null) {
            out.writeBoolean(true);
            target.toStream(out);
        } else {
            out.writeBoolean(false);
        }

        if (originUser != null) {
            out.writeBoolean(true);
            originUser.toStream(out);
        } else {
            out.writeBoolean(false);
        }

        if (targetUser != null) {
            out.writeBoolean(true);
            targetUser.toStream(out);
        } else {
            out.writeBoolean(false);
        }

        out.writeLong(createdTime);

        logger.info("IV size: " + iv.length);
        logger.info("EncryptedMessage size: " + encryptedMessage.length);

        out.write(iv);
        out.writeInt(encryptedMessage.length);
        out.write(encryptedMessage);
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

    public Node getOrigin() {
        return origin;
    }

    public void setOrigin(Node origin) {
        this.origin = origin;
    }

    public Node getTarget() {
        return target;
    }

    public User getOriginUser() {
        return originUser;
    }

    public User getTargetUser() {
        return targetUser;
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
