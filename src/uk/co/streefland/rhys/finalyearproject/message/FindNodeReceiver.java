package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.util.List;

/**
 * Receives a FindNodeMessage and sends a FindNodeReplyMessage as a reply with the K closest nodes to the NodeId provided
 */
public class FindNodeReceiver implements Receiver {

    private final Server server;
    private final LocalNode localNode;
    private final Configuration config;

    public FindNodeReceiver(Server server, LocalNode local, Configuration config) {
        this.server = server;
        this.localNode = local;
        this.config = config;
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
        FindNodeMessage msg = (FindNodeMessage) incoming;

        Node origin = msg.getOrigin();

        /* Insert origin into local routing table */
        this.localNode.getRoutingTable().insert(origin);

        /* Find nodes closest to the NodeId in local routing table */
        List<Node> nodes = this.localNode.getRoutingTable().findClosest(msg.getLookupId(), this.config.getK());

        /* Create the FindNodeReplyMessage */
        Message reply = new FindNodeReplyMessage(this.localNode.getNode(), nodes);

        /* The server sends the reply */
        if (this.server.isRunning()) {
            this.server.reply(origin, reply, communicationId);
        }
    }

    /**
     * We don't need to do anything here
     *
     * @param communicationId
     * @throws IOException
     */
    @Override
    public void timeout(int communicationId) throws IOException {
    }
}
