package uk.co.streefland.rhys.finalyearproject.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;

import java.io.IOException;

/**
 * Receives a TextMessage
 */
public class TextReceiver implements Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Server server;
    private final LocalNode localNode;

    public TextReceiver(Server server, LocalNode localNode) {
        this.server = server;
        this.localNode = localNode;
    }

    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        TextMessage msg = (TextMessage) incoming;

        if (msg.getTarget() != null) {
            if (msg.getTarget().getNodeId().equals(localNode.getNode().getNodeId())) {
                if (msg.getOriginUser() != null) {
                    logger.info("Received a message intended for me");
                    localNode.getMessages().addReceivedMessage(msg);
                }
            } else {
                /* This is a message intended for a different target - handle */
                logger.info("Received a message intended for another node");
                localNode.getMessages().addForwardMessage(msg);
            }
        } else {
            System.out.println("Broadcast message received: " + msg.getMessage());
        }

        /* Create the AcknowledgeMessage */
        Message ack = new AcknowledgeMessage(localNode.getNode(), true);

        /* The server sends the reply */
        if (server.isRunning()) {
            server.reply(msg.getOrigin(), ack, communicationId);
        }
    }

    /**
     * Nothing to be done here
     *
     * @param communicationId
     * @throws IOException
     */
    @Override
    public void timeout(int communicationId) throws IOException {
    }
}
