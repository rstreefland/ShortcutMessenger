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
 * Created by Rhys on 16/10/2016.
 */
public class BucketTest {

    KeyId key = new KeyId();
    Bucket bucket;
    Contact contact1;
    Contact contact2;
    Contact contact3;
    Contact contact4;
    Contact contact5;

    @Before
    public void setUp() throws UnknownHostException, InterruptedException {
        bucket = new Bucket(0);
    }

    @Test
    public void testInsert() throws Exception {

        contact1 = new Contact(new Node(key, InetAddress.getByName("127.0.0.1"), 123));
        contact2 = new Contact(new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 456));
        contact3 = new Contact(new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 246));
        contact4 = new Contact(new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 123));
        contact5 = new Contact(new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 123));

        bucket.insert(contact1);
        bucket.insert(contact2);
        bucket.insert(contact3);

        Thread.sleep(10);
        contact1.setSeenNow();
        bucket.insert(contact1);

        assertEquals(bucket.getNumberOfContacts(), 3);

        System.out.println(bucket.getContacts().get(0).getNode());
        System.out.println(bucket.getContacts().get(1).getNode());
        System.out.println(bucket.getContacts().get(2).getNode());

        assertEquals(bucket.getContacts().get(0), contact3);
        assertEquals(bucket.getContacts().get(1), contact2);
        assertEquals(bucket.getContacts().get(2), contact1);

        bucket.insert(contact4);

        Thread.sleep(10);
        contact5.setSeenNow();
        bucket.insert(contact5);

        assertEquals(bucket.getNumberOfContacts(), 5);
        assertEquals(bucket.getContacts().get(4), contact5 );
    }

    @Test
    public void testRemoveContact() throws Exception {

    }

    @Test
    public void testRemoveContactForceDeletion() throws Exception {

    }

}