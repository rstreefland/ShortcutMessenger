package uk.co.streefland.rhys.finalyearproject.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;

import java.io.IOException;

/**
 * Receives and handles a ConnectMessage from another node
 */
public class ConnectReceiver implements Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Server server;
    private final LocalNode localNode;

    public ConnectReceiver(Server server, LocalNode localNode) {
        this.server = server;
        this.localNode = localNode;
    }

    /**
     * Handles an incoming ConnectMessage
     *
     * @param communicationId
     * @throws java.io.IOException
     */
    @Override
    public void receive(Message incoming, int communicationId) throws IOException {

        ConnectMessage message = (ConnectMessage) incoming;

        /* Update the local routing table inserting the origin node. */
        if (localNode.getRoutingTable().insert(message.getOrigin()) == true) {
            /* only print the message if this is the first time that we've seen this node */
            logger.info("A new node has bootstrapped to this node; nodeID: {}", message.getOrigin().toString());
        }

        logger.debug("New routing table: \n {}", localNode.getRoutingTable().toString());

        /* Create the AcknowledgeMessage */
        AcknowledgeMessage msg = new AcknowledgeMessage(localNode.getNode(), true);

        /* Reply to the origin with the AcknowledgeMessage */
        server.reply(message.getOrigin(), msg, communicationId);
    }

    /**
     * Nothing to be done here
     *
     * @param communicationId
     * @throws java.io.IOException
     */
    @Override
    public void timeout(int communicationId) throws IOException {
    }
}
