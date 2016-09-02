package uk.co.streefland.rhys.finalyearproject.routing;

import uk.co.streefland.rhys.finalyearproject.node.Node;

/**
 * Stores information about the contacts of the node. Contacts are stored in buckets in the routing table
 */
public class Contact implements Comparable<Contact> {

    private final Node node;
    private long lastSeen;
    private int staleCount;

    public Contact(Node node) {
        this.node = node;
        this.lastSeen = System.currentTimeMillis() / 1000L;
    }

    @Override
    public boolean equals(Object c) {
        if (c instanceof Contact) {
            return ((Contact) c).getNode().equals(this.getNode());
        }

        return false;
    }

    @Override
    public int compareTo(Contact o) {
        if (this.getNode().equals(o.getNode())) {
            return 0;
        }

        return (lastSeen > o.getLastSeen()) ? 1 : -1;
    }

    /**
     * Updates the last seen timestamp for this contact
     */
    public void setSeenNow() {
        this.lastSeen = System.currentTimeMillis() / 1000L;
    }

    public void incrementStaleCount() {
        staleCount++;
    }

    public void resetStaleCount() {
        this.staleCount = 0;
    }

    public Node getNode() {
        return this.node;
    }

    public long getLastSeen() {
        return this.lastSeen;
    }

    public int getStaleCount() {
        return this.staleCount;
    }

    @Override
    public int hashCode() {
        return this.getNode().hashCode();
    }
}
