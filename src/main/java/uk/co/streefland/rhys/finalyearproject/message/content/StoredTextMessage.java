package uk.co.streefland.rhys.finalyearproject.message.content;

import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.operation.SendMessageOperation;

import java.io.*;

/**
 * A simple text message that's stored on the local node as part of a conversation
 */
public class StoredTextMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final KeyId messageId;
    private final String authorUser;
    private final String recipientUser;
    private final String message;
    private SendMessageOperation.Status messageStatus;
    private final long createdTime;

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

}
