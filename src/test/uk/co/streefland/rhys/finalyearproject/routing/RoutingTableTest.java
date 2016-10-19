package uk.co.streefland.rhys.finalyearproject.routing;

import org.junit.Before;
import org.junit.Test;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the RoutingTable class
 */
public class RoutingTableTest {

    RoutingTable routingTable;
    Node node1;
    Node node2;
    Node node3;
    Node node4;
    Node node5;
    Node node6;
    Node node7;

    @Before
    public void setUp() throws Exception {
        routingTable = new RoutingTable(new Node(new KeyId("a"), InetAddress.getByName("127.0.0.1"), 12345));
        node1 = new Node(new KeyId("b"),InetAddress.getByName("10.10.100.1"), 15315);
        node2 = new Node(new KeyId("c"),InetAddress.getByName("192.168.0.2"), 53273);
        node3 = new Node(new KeyId("d"),InetAddress.getByName("192.168.0.3"), 12723);
        node4 = new Node(new KeyId("e"),InetAddress.getByName("127.0.0.1"), 12345);
        node5 = new Node(new KeyId("f"),InetAddress.getByName("192.168.0.4"), 12345);
        node6 = new Node(new KeyId("g"),InetAddress.getByName("192.168.0.5"), 12345);
        node7 = new Node(new KeyId("h"),InetAddress.getByName("192.168.0.5"), 12336);
    }

    @Test
    public void testInsertNode() throws Exception {
        assertEquals(routingTable.getAllNodes().size(), 1);

        routingTable.insert(node1);

        assertFalse(routingTable.isEmpty());

        routingTable.insert(node2);
        routingTable.insert(node3);
        routingTable.insert(node4);

        assertEquals(routingTable.getAllNodes().size(), 4);
    }

    @Test
    public void testFindClosestNodes() throws Exception {
        routingTable.insert(node1);
        routingTable.insert(node2);
        routingTable.insert(node3);
        routingTable.insert(node4);
        routingTable.insert(node5);
        routingTable.insert(node6);
        routingTable.insert(node7);

        byte[] bytes = new byte[20];
        KeyId zero = new KeyId(bytes);
        List<Node> sortedSet = routingTable.findClosest(zero);

        assertEquals(sortedSet.size(), 5);

        BigInteger previous = zero.getInt();

        for (int i=0; i < sortedSet.size(); i++) {
            assertEquals(sortedSet.get(i).getNodeId().getInt().compareTo(previous), 1);
            previous = sortedSet.get(i).getNodeId().getInt();
        }
    }

    @Test
    public void testSetUnresponsiveContact() throws Exception {
        routingTable.insert(node1);
        routingTable.insert(node2);
        routingTable.insert(node3);

        routingTable.setUnresponsiveContact(node2);

        int stale = 0;

        Bucket[] buckets = routingTable.getBuckets();

        for (Bucket bucket: buckets) {
            for (Contact contact: bucket.getContacts()) {
                if (contact.getNode().equals(node2)) {
                    stale = contact.getStaleCount();
                }
            }
        }

        assertEquals(stale, 1);
    }

    @Test
    public void testSetUnresponsiveContacts() throws Exception {
        routingTable.insert(node1);
        routingTable.insert(node2);
        routingTable.insert(node3);
        routingTable.insert(node4);
        routingTable.insert(node5);
        routingTable.insert(node6);
        routingTable.insert(node7);

        routingTable.setUnresponsiveContact(node1);
        routingTable.setUnresponsiveContact(node2);
        routingTable.setUnresponsiveContact(node3);
        routingTable.setUnresponsiveContact(node4);
        routingTable.setUnresponsiveContact(node5);
        routingTable.setUnresponsiveContact(node6);
        routingTable.setUnresponsiveContact(node7);

        boolean stale = true;

        Bucket[] buckets = routingTable.getBuckets();

        for (Bucket bucket: buckets) {
            for (Contact contact: bucket.getContacts()) {
                if (contact.getStaleCount() != 1) {
                    stale = false;
                }
            }
        }

        assertTrue(stale);
    }

    @Test
    public void testGetAllNodes() throws Exception {
        routingTable.insert(node1);
        routingTable.insert(node2);
        routingTable.insert(node3);
        routingTable.insert(node4);
        routingTable.insert(node5);
        routingTable.insert(node6);
        routingTable.insert(node7);

        assertEquals(routingTable.getAllNodes().size(), 7);
    }
}