package uk.co.streefland.rhys.finalyearproject.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;
import uk.co.streefland.rhys.finalyearproject.main.Users;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;

/**
 * Receives a TextMessage
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
