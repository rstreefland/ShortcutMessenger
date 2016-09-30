package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;

/**
 * Receives a TextMessage
 */
public class CheckUserReceiver implements Receiver {

    private final Server server;
    private final LocalNode localNode;
    private final Configuration config;

    public CheckUserReceiver(Server server, LocalNode localNode, Configuration config) {
        this.server = server;
        this.localNode = localNode;
        this.config = config;
    }

    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        CheckUserMessage msg = (CheckUserMessage) incoming;

        Node origin = msg.getOrigin();

        User existingUser = localNode.getUsers().matchUser(msg.getUser());

        /* Create the CheckUserReplyMessage */
        Message reply = new CheckUserReplyMessage(localNode.getNode(), existingUser);

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
