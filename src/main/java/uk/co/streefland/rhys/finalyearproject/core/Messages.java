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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private transient LocalNode localNode;
    private Map<String, ArrayList<StoredTextMessage>> userMessages;
    private Map<KeyId, Long> receivedMessages;
    private Map<KeyId, TextMessage> forwardMessages;
    private transient ObjectProperty<StoredTextMessage> lastMessage;
    private transient ObjectProperty<StoredTextMessage> lastMessageUpdate;

    public Messages(LocalNode localNode) {
        this.localNode = localNode;
        this.userMessages = new HashMap<>();
        this.receivedMessages = new HashMap<>();
        this.forwardMessages = new HashMap<>();
        this.lastMessage = new SimpleObjectProperty<>();
        this.lastMessageUpdate = new SimpleObjectProperty<>();
    }

    public void init(LocalNode localNode) {
        this.localNode = localNode;
        this.lastMessage = new SimpleObjectProperty<>();
        this.lastMessageUpdate = new SimpleObjectProperty<>();
    }

    public void sendMessage(String message, User target) throws IOException {

        if (message.isEmpty() || localNode.getUsers().getLocalUser() == null) {
            return;
        }

        /* Current timestamp and messageId for message objects */
        long createdTime = new Date().getTime() / 1000;
        KeyId messageId = new KeyId();

        /* Create the StoredTextMessage and set the lastMessage */
        StoredTextMessage stm = new StoredTextMessage(messageId, localNode.getUsers().getLocalUser().getUserName(), target.getUserName(), message, SendMessageOperation.PENDING_DELIVERY, createdTime);
        lastMessage.set(stm);

        /* Add the storedtextmessage to an existing conversation or create a new one if it doesn't already exist */
        if (userMessages.putIfAbsent(target.getUserName(), new ArrayList(Arrays.asList(stm))) != null) {
            ArrayList<StoredTextMessage> conversation = userMessages.get(target.getUserName());
            conversation.add(stm);
        }

        /* Execute the sendmessageoperation */
        SendMessageOperation operation = new SendMessageOperation(localNode, target, messageId, message, createdTime);
        operation.execute();

        if (operation.getUser() != null) {
            localNode.getUsers().addUserToCache(operation.getUser());

            if (operation.messageStatus() != SendMessageOperation.PENDING_DELIVERY && stm.getMessageStatus() != SendMessageOperation.DELIVERED) {
                stm.setMessageStatus(operation.messageStatus());
                lastMessageUpdate.set(stm);
            }
        } else {
            logger.error("User {} doesn't exist", target.getUserName());
        }
    }

    public void setDelivered(String userName, KeyId messageId) {

        ArrayList<StoredTextMessage> conversation = userMessages.get(userName);

        /* Reverse for loop should be quicker here because message is more likely to be one of the last in the conversation */
        for (int i = conversation.size() - 1; i >= 0; i--) {
           if (conversation.get(i).getMessageId().equals(messageId)) {
               logger.info("Updating message status to delivered");
               conversation.get(i).setMessageStatus(SendMessageOperation.DELIVERED);
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
    public void addReceivedMessage(TextMessage message) throws IOException {
        Node origin = message.getOrigin();
        User originUser = message.getAuthorUser();
        String userName = originUser.getUserName();
        String messageString = message.getMessage();

        StoredTextMessage storedMessage = new StoredTextMessage(message.getMessageId(), userName, localNode.getUsers().getLocalUser().getUserName(), messageString, message.getCreatedTime());

        if (receivedMessages.putIfAbsent(message.getMessageId(), message.getCreatedTime()) == null) {
            System.out.println("Message received from " + userName + ": " + messageString);

            if (userMessages.putIfAbsent(userName, new ArrayList(Arrays.asList(storedMessage))) != null) {
                ArrayList<StoredTextMessage> conversation = userMessages.get(userName);
                conversation.add(storedMessage);
            }

            lastMessage.set(storedMessage);
            localNode.getUsers().addUserToCache(originUser, origin);

            /* If the message was forwarded - punch a hole in the NAT/firewall so we can communicate directly from now on */
            if (!origin.equals(message.getSource())) {
                logger.info("THIS IS A FORWARDED MESSAGE");
            }
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
     * Removes any forwardMessages that are older than two days
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

    @Override
    public String toString() {
        return super.toString(); // TODO: 08/10/2016  change this to print out all messages in memory
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
