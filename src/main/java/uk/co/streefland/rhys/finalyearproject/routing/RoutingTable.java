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

    private static final long serialVersionUID = 1L;

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

        /* Insert the local node */
        insert(localNode);

        isEmpty = true;
    }

    /**
     * Inserts a contact into to the routing table based on how far it is from LocalNode.
     * If the new contact has socketAddress that matches one of an existing contact then the
     * old contact is removed from the routing table before inserting the new contact
     *
     * @param n The node to add
     * @return returns true if the inserted contact is a contact that we've never seen before
     */
    public synchronized boolean insert(Node n) {
        return insert(new Contact(n));
    }

    private synchronized boolean insert(Contact c) {
        isEmpty = false;

        for (Node existingNode : getAllNodes(false)) {
            /* If node has same public+private address - replace it OR if nodeId has same nodeId replace it */
            if ((c.getNode().getPublicInetAddress().equals(existingNode.getPublicInetAddress()) && c.getNode().getPrivateInetAddress().equals(existingNode.getPrivateInetAddress())) || c.getNode().getNodeId().equals(existingNode.getNodeId())) {
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
     * Find the closest set of contacts to a given KeyId
     *
     * @param target The target KeyId
     * @return List A List of contacts closest to the target KeyId
     */
    public synchronized final List<Node> findClosest(KeyId target, boolean ignoreStale) {
        TreeSet<Node> sortedSet = new TreeSet<>(new KeyComparator(target));
        sortedSet.addAll(getAllNodes(ignoreStale));

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
     * Used to update a contact
     */
    public synchronized void refreshContact(Node n, boolean resetStaleCount) {

        if (!resetStaleCount) {
            Contact existing = getContact(n);
            if (existing != null) {
                insert(existing);
            } else {
                insert(n);
            }
        } else {
            insert(n);
        }
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

    public Contact getContact(Node node) {
        for (Contact existingContact : getAllContacts()) {
            if (node.getNodeId().equals(existingContact.getNode().getNodeId())) {
                /* Return the contact object */
                return existingContact;
            }
        }
        return null;
    }

    /**
     * @return A list of all Nodes in this RoutingTable
     */
    public synchronized final List<Node> getAllNodes(boolean ignoreStale) {
        List<Node> nodes = new ArrayList<>();

        for (Bucket b : buckets) {
            for (Contact c : b.getContacts()) {
                if (ignoreStale) {
                    if (c.getStaleCount() == 0) {
                        nodes.add(c.getNode());
                    }
                } else {
                    nodes.add(c.getNode());
                }
            }
        }

        return nodes;
    }

    /**
     * @return A list of all Contacts in this RoutingTable
     */
    private synchronized List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();

        for (Bucket b : buckets) {
            for (Contact c : b.getContacts()) {
                contacts.add(c);
            }
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

        /* If we are trying to insert a node into it's own routing table, the bucket ID will be -1, so keep it in bucket 0 */
        return bucketId < 0 ? 0 : bucketId;
    }

    /**
     * @return Bucket[] All buckets stored in this RoutingTable
     */
    public final Bucket[] getBuckets() {
        return buckets;
    }

    /**
     * Returns true if the routing table is empty, false if not
     *
     * @return
     */
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

}