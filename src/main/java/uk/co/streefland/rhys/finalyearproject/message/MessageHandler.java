package uk.co.streefland.rhys.finalyearproject.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.message.content.NotifySuccessMessage;
import uk.co.streefland.rhys.finalyearproject.message.content.NotifySuccessReceiver;
import uk.co.streefland.rhys.finalyearproject.message.content.TextMessage;
import uk.co.streefland.rhys.finalyearproject.message.content.TextReceiver;
import uk.co.streefland.rhys.finalyearproject.message.node.*;
import uk.co.streefland.rhys.finalyearproject.message.user.*;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Handles creating messages and receivers for incoming and outgoing messages and receivers based on their byte code
 */
public class MessageHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalNode localNode;

    public MessageHandler(LocalNode localNode) {
        this.localNode = localNode;
    }

    public Message createMessage(byte code, DataInputStream in) throws IOException {
        switch (code) {
            case AcknowledgeMessage.CODE:
                return new AcknowledgeMessage(in);
            case ConnectMessage.CODE:
                return new ConnectMessage(in);
            case FindNodeMessage.CODE:
                return new FindNodeMessage(in);
            case FindNodeMessageReply.CODE:
                return new FindNodeMessageReply(in);
            case TextMessage.CODE:
                return new TextMessage(in);
            case StoreUserMessage.CODE:
                return new StoreUserMessage(in);
            case VerifyUserMessage.CODE:
                return new VerifyUserMessage(in);
            case VerifyUserMessageReply.CODE:
                return new VerifyUserMessageReply(in);
            case NotifySuccessMessage.CODE:
                return new NotifySuccessMessage(in);
            case NetworkTraversalMessage.CODE:
                return new NetworkTraversalMessage(in);
            case NetworkTraversalMessageReply.CODE:
                return new NetworkTraversalMessageReply(in);
            default:
                logger.warn("No message type found for message code: {}", code);
                return null;
        }
    }

    public Receiver createReceiver(int port, byte code) {
        switch (code) {
            case ConnectMessage.CODE:
                return new ConnectReceiver(port, localNode);
            case FindNodeMessage.CODE:
                return new FindNodeReceiver(localNode);
            case TextMessage.CODE:
                return new TextReceiver(localNode);
            case StoreUserMessage.CODE:
                return new StoreUserReceiver(localNode);
            case VerifyUserMessage.CODE:
                return new VerifyUserReceiver(localNode);
            case NotifySuccessMessage.CODE:
                return new NotifySuccessReceiver(localNode);
            case NetworkTraversalMessage.CODE:
                return new NetworkTraversalReceiver(localNode);
            default:
                logger.warn("No receiver type found for message code: {}", code);
                return null;
        }
    }
}
