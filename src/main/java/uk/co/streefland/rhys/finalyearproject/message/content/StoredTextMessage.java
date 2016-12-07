package uk.co.streefland.rhys.finalyearproject.message.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Encryption;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.operation.SendMessageOperation;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * A simple text message that's stored on the local node as part of a conversation
 */
public class StoredTextMessage implements Serializable {

    private KeyId messageId;
    private String authorUser;
    private String recipientUser;
    private String message;
    private SendMessageOperation.Status messageStatus;
    private long createdTime;

    public StoredTextMessage(KeyId messageId, String authorUser, String recipientUser, String message, long createdTime) {
        this.messageId = messageId;
        this.authorUser = authorUser;
        this.recipientUser = recipientUser;
        this.message = message;
        this.createdTime = createdTime;
    }

    public StoredTextMessage(KeyId messageId, String authorUser, String recipientUser, String message, SendMessageOperation.Status messageStatus, long createdTime) {
        this.messageId = messageId;
        this.authorUser = authorUser;
        this.recipientUser = recipientUser;
        this.message = message;
        this.messageStatus = messageStatus;
        this.createdTime = createdTime;
    }

    public KeyId getMessageId() {
        return messageId;
    }

    public String getAuthor() {
        return authorUser;
    }

    public String getRecipient() {
        return recipientUser;
    }

    public String getMessage() {
        return message;
    }

    public SendMessageOperation.Status getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(SendMessageOperation.Status messageStatus) {
        this.messageStatus = messageStatus;
    }

    public long getCreatedTime() {
        return createdTime;
    }
}
