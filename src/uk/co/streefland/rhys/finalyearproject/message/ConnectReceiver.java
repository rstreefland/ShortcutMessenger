package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;

import java.io.IOException;

/**
 * Receives and handles a ConnectMessage from another node
 */
public class ConnectReceiver implements Receiver {

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
        localNode.getRoutingTable().insert(message.getOrigin());

        /* Create the AcknowledgeConnectMessage */
        AcknowledgeConnectMessage msg = new AcknowledgeConnectMessage(localNode.getNode());

        /* Reply to the origin with the AcknowledgeConnectMessage */
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
