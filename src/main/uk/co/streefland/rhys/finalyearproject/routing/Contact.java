package uk.co.streefland.rhys.finalyearproject.routing;

import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.Serializable;

/**
 * Stores information about the contacts of the node. Contacts are stored in buckets in the routing table
 */
public class Contact implements Comparable<Contact>, Cloneable, Serializable {

    private final Node node;
    private long lastSeen;
    private int staleCount;

    public Contact(Node node) {
        this.node = node;
        this.lastSeen = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object c) {
        if (c instanceof Contact) {
            return ((Contact) c).getNode().equals(getNode());
        }

        return false;
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

    @Override
    public Object clone() throws CloneNotSupportedException{
        return super.clone();
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

    @Override
    public int hashCode() {
        return getNode().hashCode();
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


}
