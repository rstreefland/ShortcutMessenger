package uk.co.streefland.rhys.finalyearproject.core;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.message.content.StoredTextMessage;
import uk.co.streefland.rhys.finalyearproject.message.content.TextMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.operation.SendMessageOperation;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Stores and manages messages on the local node
 */
public class Messages implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private transient LocalNode localNode;
    private final Map<KeyId, Long> receivedMessages;
    private final Map<KeyId, TextMessage> forwardMessages;
    private final Map<String, ArrayList<StoredTextMessage>> userMessages;

    private transient ObjectProperty<StoredTextMessage> lastMessage;
    private transient ObjectProperty<StoredTextMessage> lastMessageUpdate;

    public Messages(LocalNode localNode) {
        this.userMessages = new HashMap<>();
        this.receivedMessages = new HashMap<>();
        this.forwardMessages = new HashMap<>();

        init(localNode);
    }

    /**
     * Used to set the transient objects when a saved state has been read
     */
    public void init(LocalNode localNode) {
        this.localNode = localNode;
        this.lastMessage = new SimpleObjectProperty<>();
        this.lastMessageUpdate = new SimpleObjectProperty<>();
    }

    /**
     * Handles the process of sending a message to another user
     *
     * @param message The message to send
     * @param target  The target user
     * @throws IOException
     */
    public void sendMessage(String message, User target) throws IOException {

        if (message.isEmpty() || localNode.getUsers().getLocalUser() == null) {
            return;
        }

        /* Current timestamp and messageId for message objects */
        long createdTime = new Date().getTime() / 1000;
        KeyId messageId = new KeyId();

        /* Create the StoredTextMessage and set the lastMessage */
        StoredTextMessage stm = new StoredTextMessage(messageId, localNode.getUsers().getLocalUser().getUserName(), target.getUserName(), message, SendMessageOperation.Status.PENDING_DELIVERY, createdTime);
        lastMessage.set(stm);

        /* Add the storedtextmessage to an existing conversation or create a new one if it doesn't already exist */
        if (userMessages.putIfAbsent(target.getUserName(), new ArrayList<>(Collections.singletonList(stm))) != null) {
            ArrayList<StoredTextMessage> conversation = userMessages.get(target.getUserName());
            conversation.add(stm);
        }

        long start = System.currentTimeMillis();

        /* Execute the SendMessageOperation */
        SendMessageOperation operation = new SendMessageOperation(localNode, target, messageId, message, createdTime);
        operation.execute();

        long end = System.currentTimeMillis();
        long time = end - start;
        logger.info("OUTGOING SMO TOOK: " + time);

        if (operation.getUser() != null) {

            localNode.getUsers().addUserToCache(operation.getUser(), false);

            /* This if statement prevents the race condition */
            if (operation.messageStatus() != SendMessageOperation.Status.PENDING_DELIVERY && stm.getMessageStatus() != SendMessageOperation.Status.DELIVERED) {
                stm.setMessageStatus(operation.messageStatus());
                lastMessageUpdate.set(stm);
            }
        } else {
            logger.error("User {} doesn't exist", target.getUserName());
        }
    }

    /**
     * Updates the status of a sent message to delivered
     *
     * @param userName  The recipient of the message
     * @param messageId The id of the message to update
     */
    public void setDelivered(String userName, KeyId messageId) {

        /* Get the conversation */
        ArrayList<StoredTextMessage> conversation = userMessages.get(userName);

        /* Reverse for loop should be quicker here because message is more likely to be one of the last in the conversation */
        for (int i = conversation.size() - 1; i >= 0; i--) {
            if (conversation.get(i).getMessageId().equals(messageId)) {
                logger.info("Updating message status to delivered");
                conversation.get(i).setMessageStatus(SendMessageOperation.Status.DELIVERED);
                logger.info("New message status is: " + conversation.get(i).getMessageStatus());
                lastMessageUpdate.set(null);
                lastMessageUpdate.set(conversation.get(i));
            }
        }
    }

    /**
     * Store a received message in the receivedMessages HashMap if it doesn't already exist.
     * Print the message if it doesn't already exist
     *
     * @param message The message to store
     */
    public void addReceivedMessage(TextMessage message) {
        /* Only do anything if we haven't received this message before */
        if (receivedMessages.putIfAbsent(message.getMessageId(), message.getCreatedTime()) != null) {
            return;
        }

        Node origin = message.getOrigin();
        User originUser = message.getAuthorUser();
        String userName = originUser.getUserName();
        String messageString = message.getMessage();

        System.out.println("Message received from " + userName + ": " + messageString);

        /* Create the StoredTextMessage object */
        StoredTextMessage stm = new StoredTextMessage(message.getMessageId(), userName, localNode.getUsers().getLocalUser().getUserName(), messageString, message.getCreatedTime());
        lastMessage.set(stm);

        /* Add the StoredTextMessage to an existing conversation or create a new one if it doesn't already exist */
        if (userMessages.putIfAbsent(userName, new ArrayList<>(Collections.singletonList(stm))) != null) {
            ArrayList<StoredTextMessage> conversation = userMessages.get(userName);
            conversation.add(stm);
        }

        if (!origin.equals(message.getSource())) {
            logger.info("THIS IS A FORWARDED MESSAGE");
            /* Add the origin user and node to cache */
            localNode.getUsers().addUserToCache(originUser, false);
        } else {
            localNode.getUsers().addUserToCache(originUser, true);
        }
    }

    /**
     * Inserts a message into the forwardMessages HashMap if it doesn't already exist
     *
     * @param message The message to store
     */
    public void addForwardMessage(TextMessage message) {
        forwardMessages.putIfAbsent(message.getMessageId(), message);
    }

    /**
     * Removes any forwardMessages and receivedMessages that are older than two days
     */
    public void cleanUp() {
        long currentTime = new Date().getTime() / 1000; // current time in seconds

        /* Cleanup forward messages */
        for (Map.Entry<KeyId, TextMessage> entry : forwardMessages.entrySet()) {
            // Remove if current time is greater than created time + two days
            if (currentTime >= (entry.getValue().getCreatedTime() + Configuration.FORWARD_MESSAGE_EXPIRY)) {
                forwardMessages.remove(entry.getKey());
            }
        }

        /* Cleanup received messages */
        for (Map.Entry<KeyId, Long> entry : receivedMessages.entrySet()) {
            // Remove if current time is greater than created time + two days
            if (currentTime >= (entry.getValue() + Configuration.FORWARD_MESSAGE_EXPIRY)) {
                receivedMessages.remove(entry.getKey());
            }
        }
    }

    public Map<String, ArrayList<StoredTextMessage>> getUserMessages() {
        return userMessages;
    }

    public Map<KeyId, TextMessage> getForwardMessages() {
        return forwardMessages;
    }

    public ObjectProperty<StoredTextMessage> lastMessageProperty() {
        return lastMessage;
    }

    public ObjectProperty<StoredTextMessage> lastMessageUpdateProperty() {
        return lastMessageUpdate;
    }
}
