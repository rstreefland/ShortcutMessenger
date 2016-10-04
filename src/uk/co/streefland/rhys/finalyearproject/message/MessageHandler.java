package uk.co.streefland.rhys.finalyearproject.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Handles creating messages and receivers for incoming and outgoing messages and receivers based on their byte code
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
            case StoreUserMessage.CODE:
                return new StoreUserMessage(in);
            case VerifyUserMessage.CODE:
                return new VerifyUserMessage(in);
            case VerifyUserReplyMessage.CODE:
                return new VerifyUserReplyMessage(in);
            default:
            logger.warn("No message type found for message code: {}", code);
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
            case StoreUserMessage.CODE:
                return new StoreUserReceiver(server, localNode, config);
            case VerifyUserMessage.CODE:
                return new VerifyUserReceiver(server, localNode);
            default:
                logger.warn("No receiver type found for message code: {}", code);
                return null;
        }
    }
}
