package uk.co.streefland.rhys.finalyearproject.node;

import org.junit.Test;

import java.net.InetAddress;
import java.util.Comparator;

import static org.junit.Assert.*;

/**
 * Tests the KeyComparator class
 */
public class KeyComparatorTest {

    @Test
    public void testNodeComparison() throws Exception {
        KeyId baseKey = new KeyId("sdlkfhjsd");
        Comparator<Node> comparator = new KeyComparator(baseKey);

        Node node1 = new Node(baseKey.generateKeyIdUsingDistance(25), InetAddress.getByName("127.0.0.1"), InetAddress.getByName("127.0.0.2"), 12345, 54321);
        Node node2 = new Node(baseKey.generateKeyIdUsingDistance(9), InetAddress.getByName("127.0.0.1"), InetAddress.getByName("127.0.0.2"), 12345, 54321);
        Node node3 = new Node(baseKey.generateKeyIdUsingDistance(147), InetAddress.getByName("127.0.0.1"), InetAddress.getByName("127.0.0.2"),  12345, 54321);

        assertEquals(comparator.compare(node1, node2), 1);
        assertEquals(comparator.compare(node2, node3), -1);
        assertEquals(comparator.compare(node3, node1), 1);
        assertEquals(comparator.compare(node1, node1), 0);
    }
}