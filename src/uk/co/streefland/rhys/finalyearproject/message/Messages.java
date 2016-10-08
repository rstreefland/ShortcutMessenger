package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles messages stored on the local node
 */
public class Messages {

    private Map<KeyId, TextMessage> receivedMessages;
    private Map<KeyId, TextMessage> forwardMessages;

    LocalNode localNode;

    public Messages(LocalNode localNode) {
        this.receivedMessages = new HashMap<>();
        this.forwardMessages = new HashMap<>();

        this.localNode = localNode;
    }

    public void addReceivedMessage(TextMessage message) {
        receivedMessages.put(message.getMessageId(), message);
        System.out.println("Message received from " + message.getOriginUser().getUserName() + ": " + message.getMessage());
    }

    public void addForwardMessage(TextMessage message) {
        forwardMessages.put(message.getMessageId(), message);
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
}
