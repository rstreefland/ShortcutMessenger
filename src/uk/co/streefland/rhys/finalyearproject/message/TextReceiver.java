package uk.co.streefland.rhys.finalyearproject.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.*;
import java.util.List;

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

        Node origin = msg.getOrigin();

        System.out.println("Message received from " + origin.getSocketAddress().getHostName() + " : " + msg.getMessage());

        /* Create the AcknowledgeMessage */
        Message ack = new AcknowledgeMessage(localNode.getNode());

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
