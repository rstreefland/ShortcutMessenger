package uk.co.streefland.rhys.finalyearproject.core;

import javafx.application.Platform;
import javafx.beans.property.*;
import uk.co.streefland.rhys.finalyearproject.message.TextMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Stores and manages messages on the local node
 */
public class Messages {

    private final Map<String, ArrayList<String>> userMessages;
    private final Map<KeyId, Long> receivedMessages;
    private final Map<KeyId, TextMessage> forwardMessages;

    private ObjectProperty<String> lastMessage = new SimpleObjectProperty<>();
    private String lastMessageUser;

    public Messages() {
        this.userMessages = new HashMap<>();
        this.receivedMessages = new HashMap<>();
        this.forwardMessages = new HashMap<>();
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


        if (receivedMessages.putIfAbsent(message.getMessageId(), message.getCreatedTime()) == null) {
            System.out.println("Message received from " + userName + ": " + messageString);

            if (userMessages.putIfAbsent(userName, new ArrayList(Arrays.asList(message.getMessage()))) != null) {
                ArrayList<String> currentUserMessages = userMessages.get(userName);
                currentUserMessages.add(messageString);
            }
            lastMessageUser = userName;
            lastMessage.set(messageString);
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
    }

    @Override
    public String toString() {
        return super.toString(); // TODO: 08/10/2016  change this to print out all messages in memory
    }

    public Map<String, ArrayList<String>> getUserMessages() {
        return userMessages;
    }

    public Map<KeyId, TextMessage> getForwardMessages() {
        return forwardMessages;
    }

    public String getLastMessageUser() {
        return lastMessageUser;
    }

    public ObjectProperty<String> lastMessageProperty() {
        return lastMessage;
    }
}
