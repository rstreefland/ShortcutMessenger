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
import java.net.DatagramPacket;

/**
 * Receives and handles a ConnectMessage from another node
 */
public class ConnectReceiver implements Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalNode localNode;
    private final Server server;
    private final int port;

    public ConnectReceiver(int port, LocalNode localNode) {
        this.localNode = localNode;
        this.server = localNode.getServer();
        this.port = port;
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

        Node origin = message.getOrigin();
        AcknowledgeMessage msg;

        if (origin.getPublicPort() != port) {
            logger.info("Port of new node does not match node object - updating node object and informing node");
            origin.setPublicPort(port);

            /* Create the AcknowledgeMessage with the corrected node object */
            msg = new AcknowledgeMessage(localNode.getNode(), origin, true);
        } else {

            /* Create the AcknowledgeMessage */
            msg = new AcknowledgeMessage(localNode.getNode(), true);
        }

        /* Update the local routing table inserting the origin node. */
        if (localNode.getRoutingTable().insert(message.getOrigin())) {
            /* only print the message if this is the first time that we've seen this node */
            logger.info("A new node has bootstrapped to this node; nodeID: {}", message.getOrigin().toString());
        }

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
