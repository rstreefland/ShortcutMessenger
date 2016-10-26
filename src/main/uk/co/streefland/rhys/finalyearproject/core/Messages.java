package uk.co.streefland.rhys.finalyearproject.core;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.message.TextMessage;
import uk.co.streefland.rhys.finalyearproject.message.user.StoreUserMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.operation.SendMessageOperation;

import java.io.IOException;
import java.util.*;

/**
 * Stores and manages messages on the local node
 */
public class Messages {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LocalNode localNode;
    private final Map<String, ArrayList<StoredTextMessage>> userMessages;
    private final Map<KeyId, Long> receivedMessages;
    private final Map<KeyId, TextMessage> forwardMessages;
    private final ObjectProperty<StoredTextMessage> lastMessage;

    public Messages(LocalNode localNode) {
        this.localNode = localNode;
        this.userMessages = new HashMap<>();
        this.receivedMessages = new HashMap<>();
        this.forwardMessages = new HashMap<>();
        this.lastMessage = new SimpleObjectProperty<>();
    }

    public void sendMessage(String message, User target) throws IOException {
        if (!message.isEmpty() && localNode.getUsers().getLocalUser() != null) {
            SendMessageOperation operation = new SendMessageOperation(localNode, target, message);
            operation.execute();

            if (operation.getUser() != null) {
                logger.info("Message sent to {}", operation.getUser());
                StoredTextMessage stm = new StoredTextMessage(localNode.getUsers().getLocalUser().getUserName(), message, operation.getMessage().getCreatedTime());

                if (userMessages.putIfAbsent(target.getUserName(), new ArrayList(Arrays.asList(stm))) != null) {
                    ArrayList<StoredTextMessage> conversation = userMessages.get(target.getUserName());
                    conversation.add(stm);
                }
            } else {
                logger.error("User {} doesn't exist", target.getUserName());
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
        User originUser = message.getOriginUser();
        String userName = originUser.getUserName();
        String messageString = message.getMessage();

        StoredTextMessage storedMessage = new StoredTextMessage(userName, messageString, message.getCreatedTime());


        if (receivedMessages.putIfAbsent(message.getMessageId(), message.getCreatedTime()) == null) {
            System.out.println("Message received from " + userName + ": " + messageString);

            if (userMessages.putIfAbsent(userName, new ArrayList(Arrays.asList(storedMessage))) != null) {
                ArrayList<StoredTextMessage> conversation = userMessages.get(userName);
                conversation.add(storedMessage);
            }
            lastMessage.set(storedMessage);
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
        long twoDays = 172800;
        long currentTime = new Date().getTime() / 1000; // current time in seconds

        for (Map.Entry<KeyId, TextMessage> entry : forwardMessages.entrySet()) {
            // Remove if current time is greater than created time + two days
            if (currentTime >= (entry.getValue().getCreatedTime() + twoDays)) {
                forwardMessages.remove(entry.getKey());
            }
        }
        // TODO: 26/10/2016 Clean up received messages too
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
}
