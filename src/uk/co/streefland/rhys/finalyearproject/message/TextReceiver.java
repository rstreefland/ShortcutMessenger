package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;

import java.io.IOException;

/**
 * Receives a TextMessage
 */
public class TextReceiver implements Receiver {

    private final Server server;
    private final LocalNode localNode;
    private final Configuration config;

    public TextReceiver(Server server, LocalNode local, Configuration config) {
        this.server = server;
        this.localNode = local;
        this.config = config;
    }

    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        TextMessage msg = (TextMessage) incoming;

        System.out.println("Message received:" + msg.getMessage());

        /* Create the AcknowledgeMessage */
        Message ack = new AcknowledgeMessage(localNode.getNode(), true);

        /* The server sends the reply */
        if (server.isRunning()) {
            server.reply(msg.getOrigin(), ack, communicationId);
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
