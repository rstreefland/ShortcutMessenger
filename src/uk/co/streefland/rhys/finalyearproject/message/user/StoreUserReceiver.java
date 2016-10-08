package uk.co.streefland.rhys.finalyearproject.message.user;

import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.message.AcknowledgeMessage;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;

/**
 * Receives a StoreUserMessage
 */
public class StoreUserReceiver implements Receiver {

    private final Server server;
    private final LocalNode localNode;
    private final Configuration config;

    public StoreUserReceiver(Server server, LocalNode localNode, Configuration config) {
        this.server = server;
        this.localNode = localNode;
        this.config = config;
    }

    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        StoreUserMessage msg = (StoreUserMessage) incoming;

        Node origin = msg.getOrigin();

        boolean success = localNode.getUsers().addUser(msg.getUser());

        /* Create the AcknowledgeMessage */
        Message ack = new AcknowledgeMessage(localNode.getNode(), success);

        /* The server sends the reply */
        if (server.isRunning()) {
            server.reply(origin, ack, communicationId);
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
