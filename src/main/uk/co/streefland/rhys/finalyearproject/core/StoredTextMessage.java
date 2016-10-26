package uk.co.streefland.rhys.finalyearproject.core;

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
 * A simple text message that's stored as part of a conversation
 */
public class StoredTextMessage {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String author;
    private String recipient;
    private String message;
    private long createdTime;

    /**
     * Constructor for broadcast messages
     */
    public StoredTextMessage(String author, String recipient, String message, long createdTime) {
        this.author = author;
        this.recipient = recipient;
        this.message = message;
        this.createdTime = createdTime;
    }

    public String getAuthor() {
        return author;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public long getCreatedTime() {
        return createdTime;
    }
}
