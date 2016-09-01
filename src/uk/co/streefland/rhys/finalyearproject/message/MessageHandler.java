package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Handles creating messages and receivers for incoming and outgoing messages
 */
public class MessageHandler {
    private final LocalNode localNode;
    private final Configuration config;

    public MessageHandler(LocalNode localNode, Configuration config) {
        this.localNode = localNode;
        this.config = config;
    }

    public Message createMessage(byte code, DataInputStream in) throws IOException {
        switch (code) {
            case AcknowledgeConnectMessage.CODE:
                return new AcknowledgeConnectMessage(in);
            case ConnectMessage.CODE:
                return new ConnectMessage(in);
            case FindNodeMessage.CODE:
                return new FindNodeMessage(in);
            case FindNodeReplyMessage.CODE:
                return new FindNodeReplyMessage(in);
            default:
                System.out.println(this.localNode + " - No Message handler found for message. Code: " + code);
                //return new SimpleMessage(in); todo is this required?
                return null;
        }
    }

    public Receiver createReceiver(byte code, Server server) {
        switch (code) {
            case ConnectMessage.CODE:
                return new ConnectReceiver(server, this.localNode);
            case FindNodeMessage.CODE:
                return new FindNodeReceiver(server, this.localNode, this.config);
            default:
                System.out.println("No receiver found for message. Code: " + code);
                //return new SimpleReceiver(); todo is this required?
                return null;
        }
    }
}
