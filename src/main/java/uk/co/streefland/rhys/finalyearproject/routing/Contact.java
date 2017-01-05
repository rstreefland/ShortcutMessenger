package uk.co.streefland.rhys.finalyearproject.routing;

import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.Serializable;

/**
 * Stores information about the contacts of the node. Contacts are stored in buckets in the routing table
 */
public class Contact implements Comparable<Contact>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Node node;
    private long lastSeen;
    private int staleCount;

    public Contact(Node node) {
        this.node = node;
        this.lastSeen = System.currentTimeMillis();
    }

    /**
     * Updates the last seen timestamp for this contact
     */
    public void setSeenNow() {
        lastSeen = System.currentTimeMillis();
    }

    public void incrementStaleCount() {
        staleCount++;
    }

    public void resetStaleCount() {
        staleCount = 0;
    }

    public Node getNode() {
        return node;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public int getStaleCount() {
        return staleCount;
    }

    @Override
    public int hashCode() {
        return getNode().hashCode();
    }

    @Override
    public boolean equals(Object c) {
        return c instanceof Contact && ((Contact) c).getNode().equals(getNode());

    }

    @Override
    public int compareTo(Contact o) {
        if (this.getNode().equals(o.getNode())) {
            return 0;
        } else {
            /* We may have 2 different contacts with same last seen values so we can't return 0 here */

            return this.getLastSeen() > o.getLastSeen() ? 1 : -1;
        }
    }
}
