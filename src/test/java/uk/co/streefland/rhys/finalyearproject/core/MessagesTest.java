package uk.co.streefland.rhys.finalyearproject.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.co.streefland.rhys.finalyearproject.message.content.TextMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.net.InetAddress;

import static org.junit.Assert.*;

/**
 * Tests the Messages class
 */
public class MessagesTest {

    LocalNode localNode;
    Messages messages;

    Node originNode;
    Node intermediaryNode;
    Node targetNode;

    User originUser;
    User intermediaryUser;
    User targetUser;

    TextMessage message1;
    TextMessage message2;
    TextMessage message3;

    @Before
    public void setUp() throws Exception {
        localNode = new LocalNode(new KeyId(), 12345);
        messages = new Messages(localNode);

        /* originNode = new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 123);
        intermediaryNode = new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 124);
        targetNode = new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 125); */

        originUser = new User("test1", "123");
        intermediaryUser = new User("test2", "abc");
        targetUser = new User("test3", "k!zl2dfha.sd2");

        //message1 = new TextMessage(new KeyId(), originNode, targetNode, originUser, targetUser, "hello");
        //message2 = new TextMessage(new KeyId(), originNode, targetNode, originUser, targetUser, "");
        //message3 = new TextMessage(new KeyId(), originNode, targetNode, originUser, targetUser, "sdjfhdlkfhwklehfpiwehfwbhefhnkejfhewkjhf");
    }

   /* @Test
    public void testAddReceivedMessage() throws Exception {

        messages.addReceivedMessage(message1);
        messages.addReceivedMessage(message2);
        messages.addReceivedMessage(message3);
        messages.addReceivedMessage(message1);

        assertEquals(messages.getUserMessages().size(), 3);

        assertEquals(messages.getUserMessages().get(message1.getMessageId()), message1);
        assertEquals(messages.getUserMessages().get(message2.getMessageId()), message2);
        assertEquals(messages.getUserMessages().get(message3.getMessageId()), message3);

        localNode.shutdown();
    } */

    @Test
    public void testAddForwardMessage() throws Exception {
        messages.addForwardMessage(message1);
        messages.addForwardMessage(message2);
        messages.addForwardMessage(message3);
        messages.addForwardMessage(message1);

        assertEquals(messages.getForwardMessages().size(), 3);

        assertEquals(messages.getForwardMessages().get(message1.getMessageId()), message1);
        assertEquals(messages.getForwardMessages().get(message2.getMessageId()), message2);
        assertEquals(messages.getForwardMessages().get(message3.getMessageId()), message3);

        localNode.shutdown(true);
    }

    @Test
    public void testCleanUp() throws Exception {
        messages.addForwardMessage(message1);
        messages.addForwardMessage(message2);
        messages.addForwardMessage(message3);
        messages.addForwardMessage(message1);

        messages.cleanUp();

        assertEquals(messages.getForwardMessages().size(), 3);

        assertEquals(messages.getForwardMessages().get(message1.getMessageId()), message1);
        assertEquals(messages.getForwardMessages().get(message2.getMessageId()), message2);
        assertEquals(messages.getForwardMessages().get(message3.getMessageId()), message3);

        localNode.shutdown(true);
    }
}