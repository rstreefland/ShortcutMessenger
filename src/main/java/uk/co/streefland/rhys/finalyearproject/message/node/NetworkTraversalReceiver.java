package uk.co.streefland.rhys.finalyearproject.message.node;

import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.util.List;

/**
 * Receives a NetworkTraversalMessage and sends a NetworkTraversalMessageReply
 */
public class NetworkTraversalReceiver implements Receiver {

    private final LocalNode localNode;
    private final Server server;

    public NetworkTraversalReceiver(LocalNode localNode) {
        this.localNode = localNode;
        this.server = localNode.getServer();
    }

    /**
     * Handle receiving a FindNodeMessage
     * Find the set of K nodes closest to the lookup ID and return them
     *
     * @param incoming
     * @param communicationId
     * @throws IOException
     */
    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        NetworkTraversalMessage msg = (NetworkTraversalMessage) incoming;
        Node origin = msg.getOrigin();

        List<Node> nodes = localNode.getRoutingTable().getAllNodes(false);

        /* Create the NetworkTraversalMessageReply */
        Message reply = new NetworkTraversalMessageReply(localNode.getNetworkId(), localNode.getNode(), nodes);

        /* The server sends the reply */
        if (server.isRunning()) {
            server.reply(origin, reply, communicationId);
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
