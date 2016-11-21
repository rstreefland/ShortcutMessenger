package uk.co.streefland.rhys.finalyearproject.message.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.message.AcknowledgeMessage;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;

/**
 * Receives and handles a ConnectMessage from another node
 */
public class PingReceiver implements Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalNode localNode;
    private final Server server;

    public PingReceiver(LocalNode localNode) {
        this.localNode = localNode;
        this.server = localNode.getServer();
    }

    /**
     * Handles an incoming ConnectMessage
     *
     * @param communicationId
     * @throws IOException
     */
    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        PingMessage message = (PingMessage) incoming;

        logger.info("HOLE PUNCH MESSAGE RECEIVED AND ACK'ed");

        /* Create the AcknowledgeMessage */
        AcknowledgeMessage msg = new AcknowledgeMessage(localNode.getNode(), true);

        /* Reply to the origin with the AcknowledgeMessage */
        server.reply(message.getOrigin(), msg, communicationId);
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
