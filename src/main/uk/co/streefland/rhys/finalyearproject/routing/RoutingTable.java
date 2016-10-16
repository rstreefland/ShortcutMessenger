package uk.co.streefland.rhys.finalyearproject.routing;

import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.node.KeyComparator;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * The routing table that contains all of the buckets containing all of the contacts known to the LocalNode
 */
public class RoutingTable implements Serializable {

    private final Node localNode;
    private Bucket[] buckets;
    private boolean isEmpty;

    public RoutingTable(Node localNode) {
        this.localNode = localNode;

        /* Initialise all of the buckets to a specific depth */
        this.buckets = new Bucket[KeyId.ID_LENGTH];
        for (int i = 0; i < KeyId.ID_LENGTH; i++) {
            buckets[i] = new Bucket(i);
        }

        /* Inset the local node */
        insert(localNode);

        isEmpty = true;
    }

    /**
     * Inserts a contact into to the routing table based on how far it is from LocalNode.
     * If the new contact has socketAddress that matches one of an existing contact then the
     * old contact is removed from the routing table before inserting the new contact
     *
     * @param c The contact to add
     * @return returns true if the inserted contact is a contact that we've never seen before
     */
    private synchronized boolean insert(Contact c) {
        isEmpty = false;

        for (Node existingNode : getAllNodes()) {
            if (c.getNode().getSocketAddress().equals((existingNode.getSocketAddress()))) {
                /* Get the bucket of the node */
                int bucketId = getBucketId(existingNode.getNodeId());
                /* Force remove the contact from the bucket */
                buckets[bucketId].removeContact(existingNode, true);
                buckets[getBucketId(c.getNode().getNodeId())].insert(c);
                return false;
            }
        }
        buckets[getBucketId(c.getNode().getNodeId())].insert(c);
        return true;
    }

    /**
     * Inserts a node into to the routing table based on how far it is from LocalNode.
     *
     * @param n The node to add
     * @return returns true if the inserted node is a node that we've never seen before
     */
    public synchronized final boolean insert(Node n) {
        return insert(new Contact(n));
    }

    /**
     * Find the closest set of contacts to a given KeyId
     *
     * @param target           The target KeyId
     * @return List A List of contacts closest to the target KeyId
     */
    public synchronized final List<Node> findClosest(KeyId target) {
        TreeSet<Node> sortedSet = new TreeSet<>(new KeyComparator(target));
        sortedSet.addAll(getAllNodes());

        List<Node> closest = new ArrayList<>(Configuration.K);

        /* Now we have the sorted set, lets get the top numRequired */
        int count = 0;
        for (Node n : sortedSet) {
            closest.add(n);
            if (++count == Configuration.K) {
                break;
            }
        }
        return closest;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public synchronized final String toString() {
        StringBuilder sb = new StringBuilder("\n****** Routing Table ******");
        int totalContacts = 0;
        for (Bucket b : buckets) {
            if (b.getNumberOfContacts() > 0) {
                totalContacts += b.getNumberOfContacts();
                sb.append("\n");
                sb.append(b.toString());
            }
        }

        sb.append("\nTotal Contacts: ");
        sb.append(totalContacts);
        sb.append("\n");

        sb.append("****** Routing Table Ended ******\n");
        return sb.toString();
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
     * Calculate the bucket ID in which a given node should be placed based on the distance from the local node
     *
     * @param nodeId The target KeyId
     * @return Integer The bucket id in which the given node should be placed.
     */
    private int getBucketId(KeyId nodeId) {
        int bucketId = localNode.getNodeId().getDistance(nodeId) - 1;

        /* If we are trying to insert a node into it's own routing table, then the bucket ID will be -1, so let's just keep it in bucket 0 */
        return bucketId < 0 ? 0 : bucketId;
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
        this.buckets = buckets;
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
}