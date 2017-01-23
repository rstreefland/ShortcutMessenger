package uk.co.streefland.rhys.finalyearproject.core;

import org.junit.Before;
import org.junit.Test;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;

import static org.junit.Assert.*;

/**
 * Tests the User class
 */
public class UserTest {

    private User user;

    @Before
    public void setUp() {
        user = new User("test");
    }

    @Test
    public void testStream() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        user.toStream(out);

        byte[] data = bout.toByteArray();

        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bin);

        User newUser = new User(in);

        assertEquals(user.getUserId(), newUser.getUserId());
        assertEquals(user.getUserName(), newUser.getUserName());
        //assertArrayEquals(user.getPasswordHash(), newUser.getPasswordHash());
        assertEquals(user.getAssociatedNode(), newUser.getAssociatedNode());
        assertEquals(user.getRegisterTime(), newUser.getRegisterTime());
        assertEquals(user.getLastActiveTime(), newUser.getLastActiveTime());
    }
/*
    @Test
    public void testDoPasswordsMatch() throws Exception {
        assertFalse(user.doPasswordsMatch(""));
        assertTrue(user.doPasswordsMatch("123"));
        assertFalse(user.doPasswordsMatch("abc"));
        assertFalse(user.doPasswordsMatch("="));
    } */ // TODO: 23/01/2017 fix

    @Test
    public void testAddAssociatedNode() throws Exception {
        Node node = new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), InetAddress.getByName("127.0.0.2"), 12345, 54321);
        Node node2 = new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), InetAddress.getByName("127.0.0.2"), 12345, 54321);

        user.addAssociatedNode(node);
        assertEquals(user.getAssociatedNode(), node);

        user.addAssociatedNode(node2);
        assertEquals(user.getAssociatedNode(), node2);
    }
}