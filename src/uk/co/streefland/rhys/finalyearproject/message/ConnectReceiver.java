package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;

import java.io.IOException;

/**
 * Created by Rhys on 30/08/2016.
 */
public class ConnectReceiver implements Receiver {
    private final Server server;
    private final LocalNode localNode;

    public ConnectReceiver(Server server, LocalNode localNode) {
        this.server = server;
        this.localNode = localNode;
    }

    /**
     * Handle receiving a ConnectMessage
     *
     * @param comm
     *
     * @throws java.io.IOException
     */
    @Override
    public void receive(Message incoming, int comm) throws IOException
    {
        ConnectMessage mess = (ConnectMessage) incoming;

        /* Update the local space by inserting the origin node. */
        this.localNode.getRoutingTable().insert(mess.getOrigin());

        /* Respond to the connect request */
        AcknowledgeMessage msg = new AcknowledgeMessage(this.localNode.getNode());

        /* Reply to the connect message with an Acknowledgement */
        this.server.reply(mess.getOrigin(), msg, comm);
    }

    /**
     * We don't need to do anything here
     *
     * @param comm
     *
     * @throws java.io.IOException
     */
    @Override
    public void timeout(int comm) throws IOException
    {
    }
}
