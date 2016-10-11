package uk.co.streefland.rhys.finalyearproject.core;

import uk.co.streefland.rhys.finalyearproject.message.TextMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores and manages messages on the local node
 */
public class Messages {

    private final Map<KeyId, TextMessage> receivedMessages;
    private final Map<KeyId, TextMessage> forwardMessages;

    public Messages() {
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
        if (receivedMessages.putIfAbsent(message.getMessageId(), message) == null) {
            System.out.println("Message received from " + message.getOriginUser().getUserName() + ": " + message.getMessage());
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

    public Map<KeyId, TextMessage> getForwardMessages() {
        return forwardMessages;
    }
}
