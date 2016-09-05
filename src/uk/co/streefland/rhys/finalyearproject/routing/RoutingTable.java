package uk.co.streefland.rhys.finalyearproject.routing;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.node.KeyComparator;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * The routing table that contains all of the buckets containing all of the contacts known to the LocalNode
 */
public class RoutingTable {

    private final Node localNode;
    private transient Bucket[] buckets;
    private transient Configuration config;

    public RoutingTable(Node localNode, Configuration config) {
        this.localNode = localNode;
        this.config = config;

        /* Initialise all of the buckets to a specific depth */
        this.buckets = new Bucket[NodeId.ID_LENGTH];
        for (int i = 0; i < NodeId.ID_LENGTH; i++) {
            buckets[i] = new Bucket(i, this.config);
        }

        /* Inset the local node */
        insert(localNode);
    }

    /**
     * Calculate the bucket ID in which a given node should be placed based on the distance from the local node
     *
     * @param nodeId The target NodeId
     * @return Integer The bucket id in which the given node should be placed.
     */
    public final int getBucketId(NodeId nodeId) {
        int bucketId = localNode.getNodeId().getDistance(nodeId) - 1;

        /* If we are trying to insert a node into it's own routing table, then the bucket ID will be -1, so let's just keep it in bucket 0 */
        return bucketId < 0 ? 0 : bucketId;
    }

    /**
     * Find the closest set of contacts to a given NodeId
     *
     * @param target           The target NodeId
     * @param numNodesRequired The number of contacts to return
     * @return List A List of contacts closest to the target NodeId
     */
    public synchronized final List<Node> findClosest(NodeId target, int numNodesRequired) {
        TreeSet<Node> sortedSet = new TreeSet<>(new KeyComparator(target));
        sortedSet.addAll(getAllNodes());

        List<Node> closest = new ArrayList<>(numNodesRequired);

        /* Now we have the sorted set, lets get the top numRequired */
        int count = 0;
        for (Node n : sortedSet) {
            closest.add(n);
            if (++count == numNodesRequired) {
                break;
            }
        }
        return closest;
    }

    /**
     * Inserts a contact into to the routing table based on how far it is from LocalNode.
     *
     * @param c The contact to add
     */
    public synchronized final void insert(Contact c) {
        for (Node existingNode : getAllNodes()) {
            if (c.getNode().getSocketAddress().equals((existingNode.getSocketAddress()))) {
                /* Get the bucket of the node */
                int bucketId = getBucketId(existingNode.getNodeId());
                /* Remove the contact from the bucket */
                buckets[bucketId].removeContact(existingNode, true);
            }
        }
        buckets[getBucketId(c.getNode().getNodeId())].insert(c);
    }

    /**
     * Inserts a node into to the routing table based on how far it is from LocalNode.
     *
     * @param n The node to add
     */
    public synchronized final void insert(Node n) {
        for (Node existingNode : getAllNodes()) {
            if (n.getSocketAddress().equals(existingNode.getSocketAddress())) {
                /* Get the bucket of the node */
                int bucketId = getBucketId(existingNode.getNodeId());
                /* Remove the node from the bucket */
                buckets[bucketId].removeContact(existingNode, true);
            }
        }
        buckets[getBucketId(n.getNodeId())].insert(n);
    }

    /**
     * @return A list of all Nodes in this RoutingTable
     */
    public synchronized final List<Node> getAllNodes() {
        List<Node> nodes = new ArrayList<>();

        for (Bucket b : buckets) {
            for (Contact c : b.getContacts()) {
                nodes.add(c.getNode());
            }
        }

        return nodes;
    }

    /**
     * @return List A List of all Contacts in this RoutingTable
     */
    public final List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();

        for (Bucket b : buckets) {
            contacts.addAll(b.getContacts());
        }

        return contacts;
    }

    /**
     * @return Bucket[] All buckets stored in this RoutingTable
     */
    public final Bucket[] getBuckets() {
        return buckets;
    }

    /**
     * Set all Buckets of this routing table
     *
     * @param buckets
     */
    public final void setBuckets(Bucket[] buckets) {
        buckets = buckets;
    }

    /**
     * Method used by operations to notify the routing table of any contacts that have been unresponsive.
     *
     * @param contacts The set of unresponsive contacts
     */
    public void setUnresponsiveContacts(List<Node> contacts) {
        if (contacts.isEmpty()) {
            return;
        }
        for (Node n : contacts) {
            setUnresponsiveContact(n);
        }
    }

    /**
     * Method used by operations to notify the routing table of any contacts that have been unresponsive.
     *
     * @param n The unresponsive node
     */
    public synchronized void setUnresponsiveContact(Node n) {
        int bucketId = getBucketId(n.getNodeId());

        /* Remove the contact from the bucket */
        buckets[bucketId].removeContact(n, false);
    }

    @Override
    public synchronized final String toString() {
        StringBuilder sb = new StringBuilder("\n****** Routing Table ******\n");
        int totalContacts = 0;
        for (Bucket b : buckets) {
            if (b.getNumberOfContacts() > 0) {
                totalContacts += b.getNumberOfContacts();
                sb.append("\n");
                sb.append(b.toString());
                sb.append("\n");
            }
        }

        sb.append("\nTotal Contacts: ");
        sb.append(totalContacts);
        sb.append("\n");

        sb.append("****** Routing Table Ended ******");
        return sb.toString();
    }
}