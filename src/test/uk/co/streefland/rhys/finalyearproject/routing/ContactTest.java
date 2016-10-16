package uk.co.streefland.rhys.finalyearproject.routing;

import org.junit.Before;
import org.junit.Test;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

/**
 * Tests the Contact class
 */
public class ContactTest {

    KeyId key = new KeyId();
    Contact contact1;
    Contact contact2;
    Contact contact3;

    @Before
    public void setUp() throws UnknownHostException {
        contact1 = new Contact(new Node(key, InetAddress.getByName("127.0.0.1"), 123));
        contact2 = new Contact(new Node(key, InetAddress.getByName("127.0.0.1"), 456));
        contact3 = new Contact(new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 123));
    }

    @Test
    public void equals() throws Exception {
        assertTrue(contact1.equals(contact1));
        assertTrue(contact1.equals(contact2));
        assertFalse(contact2.equals(contact3));
        assertFalse(contact3.equals(contact1));
    }

    @Test
    public void testCompareTo() throws Exception {
        contact1.setSeenNow();
        Thread.sleep(10);
        contact2.setSeenNow();
        Thread.sleep(10);
        contact3.setSeenNow();

        assertEquals(contact1.compareTo(contact1), 0);
        assertEquals(contact1.compareTo(contact2), 0);
        assertEquals(contact2.compareTo(contact3), -1);
        assertEquals(contact3.compareTo(contact1), 1);
    }

    @Test
    public void testSetSeenNow() throws Exception {
        long now = System.currentTimeMillis();

        contact1.setSeenNow();
        assertEquals(contact1.getLastSeen(), now);
    }

    @Test
    public void testStaleCount() throws Exception {
        contact1.incrementStaleCount();
        contact1.incrementStaleCount();

        assertEquals(contact1.getStaleCount(), 2);

        contact1.resetStaleCount();

        assertEquals(contact1.getStaleCount(), 0);
    }

}