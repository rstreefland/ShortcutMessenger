package uk.co.streefland.rhys.finalyearproject.routing;

import org.junit.Before;
import org.junit.Test;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the Bucket class
 */
public class BucketTest {

    KeyId key = new KeyId();
    Bucket bucket;
    Contact contact1;
    Contact contact2;
    Contact contact3;
    Contact contact4;
    Contact contact5;
    Contact contact6;
    Contact contact7;

    @Before
    public void setUp() throws UnknownHostException, InterruptedException {
        bucket = new Bucket(0);

        /* contact1 = new Contact(new Node(key, InetAddress.getByName("127.0.0.1"), 123));
        Thread.sleep(10);
        contact2 = new Contact(new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 456));
        Thread.sleep(10);
        contact3 = new Contact(new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 246));
        Thread.sleep(10);
        contact4 = new Contact(new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 123));
        Thread.sleep(10);
        contact5 = new Contact(new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 123));
        Thread.sleep(10);
        contact6 = new Contact(new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 123));
        Thread.sleep(10);
        contact7 = new Contact(new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 123)); */
    }

    @Test
    public void testInsert() throws Exception {

        bucket.insert(contact1);
        bucket.insert(contact2);
        bucket.insert(contact3);

        assertEquals(bucket.getNumberOfContacts(), 3);

        assertEquals(bucket.getContacts().get(0), contact1);
        assertEquals(bucket.getContacts().get(1), contact2);
        assertEquals(bucket.getContacts().get(2), contact3);

        bucket.insert(contact4);

        Thread.sleep(10);
        contact5.setSeenNow();
        bucket.insert(contact5);

        assertEquals(bucket.getNumberOfContacts(), 5);
        assertEquals(bucket.getContacts().get(4), contact5);

        bucket.insert(contact6);
        assertEquals(bucket.getNumberOfContacts(), 5);
    }

    @Test
    public void testRemoveContact() throws Exception {
        bucket.insert(contact1);
        bucket.insert(contact2);
        bucket.insert(contact3);

        assertEquals(bucket.removeContact(contact1.getNode(), false), false);
        assertEquals(bucket.getNumberOfContacts(), 3);

        assertEquals(bucket.removeContact(contact4.getNode(), false), false);

        bucket.insert(contact4);
        bucket.insert(contact5);
        bucket.insert(contact6);
        bucket.insert(contact7);

        assertEquals(bucket.removeContact(contact2.getNode(), false), true);
    }

    @Test
    public void testRemoveContactForceDeletion() throws Exception {
        bucket.insert(contact1);
        bucket.insert(contact2);
        bucket.insert(contact3);

        assertEquals(bucket.removeContact(contact3.getNode(), true), true);
        assertEquals(bucket.getNumberOfContacts(), 2);
    }
}