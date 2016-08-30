package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Handles creating messages and receivers
 *
 * @author Joshua Kissoon
 * @since 20140202
 */
public class MessageFactory
{

    private final LocalNode localNode;
   // private final KademliaDHT dht;
    private final Configuration config;

    public MessageFactory(LocalNode localNode, Configuration config)
    {
        this.localNode = localNode;
        //this.dht = dht;
        this.config = config;
    }

    public Message createMessage(byte code, DataInputStream in) throws IOException
    {
        switch (code)
        {
            case AcknowledgeMessage.CODE:
                return new AcknowledgeMessage(in);
            case ConnectMessage.CODE:
                return new ConnectMessage(in);
           // case ContentMessage.CODE:
           //     return new ContentMessage(in);
           // case ContentLookupMessage.CODE:
           //     return new ContentLookupMessage(in);
            case FindNodeMessage.CODE:
                return new FindNodeMessage(in);
            case NodeReplyMessage.CODE:
                return new NodeReplyMessage(in);
            //case SimpleMessage.CODE:
            //    return new SimpleMessage(in);
            //case StoreContentMessage.CODE:
            //    return new StoreContentMessage(in); */
            default:
                System.out.println(this.localNode + " - No Message handler found for message. Code: " + code);
                //return new SimpleMessage(in);
                return null;
        }
    }

    public Receiver createReceiver(byte code, Server server)
    {
        switch (code)
        {
            case ConnectMessage.CODE:
                return new ConnectReceiver(server, this.localNode);
            //case ContentLookupMessage.CODE:
            //    return new ContentLookupReceiver(server, this.localNode, this.dht, this.config);
            case FindNodeMessage.CODE:
                return new FindNodeReceiver(server, this.localNode, this.config);
            //case StoreContentMessage.CODE:
            //    return new StoreContentReceiver(server, this.localNode, this.dht);
            default:
                System.out.println("No receiver found for message. Code: " + code);
                //return new SimpleReceiver();
                return null;
        }
    }
}
