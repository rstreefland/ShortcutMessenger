package uk.co.streefland.rhys.finalyearproject.message.user;

import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;

/**
 * Receives a VerifyUserMessage
 */
public class VerifyUserReceiver implements Receiver {

    private final LocalNode localNode;
    private final Server server;

    public VerifyUserReceiver(LocalNode localNode) {
        this.localNode = localNode;
        this.server = localNode.getServer();
    }

    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        VerifyUserMessage msg = (VerifyUserMessage) incoming;
        Node origin = msg.getOrigin();

        User existingUser;

        /* Should we verify the user object or do we just need to check the userName matches */
        if (msg.isVerify()) {
            existingUser = localNode.getUsers().matchUser(msg.getUser());
        } else {
            existingUser = localNode.getUsers().findUser(msg.getUser().getUserName());
        }

        /* Create the VerifyUserReplyMessage */
        Message reply = new VerifyUserReplyMessage(localNode.getNode(), existingUser);

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