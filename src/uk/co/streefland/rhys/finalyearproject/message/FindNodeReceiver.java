package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.util.List;

/**
 * Receives a FindNodeMessage and sends a NodeReplyMessage as reply with the K-Closest nodes to the ID sent.
 *
 * @author Joshua Kissoon
 * @created 20140219
 */
public class FindNodeReceiver implements Receiver
{

    private final Server server;
    private final LocalNode localNode;
    private final Configuration config;

    public FindNodeReceiver(Server server, LocalNode local, Configuration config)
    {
        this.server = server;
        this.localNode = local;
        this.config = config;
    }

    /**
     * Handle receiving a FindNodeMessage
     * Find the set of K nodes closest to the lookup ID and return them
     *
     * @param comm
     *
     * @throws IOException
     */
    @Override
    public void receive(Message incoming, int comm) throws IOException
    {
        FindNodeMessage msg = (FindNodeMessage) incoming;

        Node origin = msg.getOrigin();

        /* Update the local space by inserting the origin node. */
        this.localNode.getRoutingTable().insert(origin);

        /* Find nodes closest to the LookupId */
        List<Node> nodes = this.localNode.getRoutingTable().findClosest(msg.getLookupId(), this.config.k());

        /* Respond to the FindNodeMessage */
        Message reply = new NodeReplyMessage(this.localNode.getNode(), nodes);
        // TODO: 30/08/2016  add the nodereplymessage functionality

        if (this.server.isRunning())
        {
            /* Let the Server send the reply */
            this.server.reply(origin, reply, comm);
        }
    }

    /**
     * We don't need to do anything here
     *
     * @param comm
     *
     * @throws IOException
     */
    @Override
    public void timeout(int comm) throws IOException
    {
    }
}
