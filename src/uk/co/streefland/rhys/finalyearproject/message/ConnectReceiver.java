package uk.co.streefland.rhys.finalyearproject.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;

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

        logger.info("A new node has bootstrapped to this node; nodeID: {}", message.getOrigin().toString());

        /* Update the local routing table inserting the origin node. */
        localNode.getRoutingTable().insert(message.getOrigin());

        logger.debug("New routing table: \n {}", localNode.getRoutingTable().toString());

        /* Create the AcknowledgeMessage */
        AcknowledgeMessage msg = new AcknowledgeMessage(localNode.getNode());

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
