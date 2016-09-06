package uk.co.streefland.rhys.finalyearproject.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Handles creating messages and receivers for incoming and outgoing messages
 */
public class MessageHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LocalNode localNode;
    private final Configuration config;

    public MessageHandler(LocalNode localNode, Configuration config) {
        this.localNode = localNode;
        this.config = config;
    }

    public Message createMessage(byte code, DataInputStream in) throws IOException {
        switch (code) {
            case AcknowledgeMessage.CODE:
                return new AcknowledgeMessage(in);
            case ConnectMessage.CODE:
                return new ConnectMessage(in);
            case FindNodeMessage.CODE:
                return new FindNodeMessage(in);
            case FindNodeReplyMessage.CODE:
                return new FindNodeReplyMessage(in);
            case TextMessage.CODE:
                return new TextMessage(in);
            default:
                logger.error("No message type found for message code: {}", code);
                return null;
        }
    }

    public Receiver createReceiver(byte code, Server server) {
        switch (code) {
            case ConnectMessage.CODE:
                return new ConnectReceiver(server, localNode);
            case FindNodeMessage.CODE:
                return new FindNodeReceiver(server, localNode, config);
            case TextMessage.CODE:
                return new TextReceiver(server,localNode, config);
            default:
                logger.error("No receiver type found for message code: {}", code);
                return null;
        }
    }
}
