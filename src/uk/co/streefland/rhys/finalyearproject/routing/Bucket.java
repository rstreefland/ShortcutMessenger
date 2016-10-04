package uk.co.streefland.rhys.finalyearproject.routing;

import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

/**
 * A bucket in the Routing Table
 */
public class Bucket implements Serializable {

    /* Depth of the bucket in the RoutingTable*/
    private final int depth;

    /* Contacts stored in this bucket */
    private TreeSet<Contact> contacts = new TreeSet<>();

    /* A set of recently seen contacts that can replace any contact that is unresponsive in the core set */
    private TreeSet<Contact> replacementCache = new TreeSet<>();

    private transient Configuration config;

    public Bucket(int depth, Configuration config) {
        this.depth = depth;
        this.config = config;
    }

    /**
     * Inserts a new contact into the bucket. If the bucket is full and there are no stale contacts to replace, the new contact will be inserted into the replacement cachr
     *
     * @param contact The contact to insert into the bucket
     */
    public synchronized void insert(Contact contact) {
        /* If contact is already in bucket */
        if (contacts.contains(contact)) {
            Contact newContact = removeContactForce(contact.getNode()); /* Remove from the TreeSet */
            newContact.setSeenNow();    /* Update the last seen time*/
            newContact.resetStaleCount();   /* Reset the stale count */
            contacts.add(newContact); /* Re-add to the TreeSet so the set is sorted correctly */
        } else {
            /* If the bucket is filled, put the contact into the replacement cache */
            if (contacts.size() >= config.getK()) {
                Contact mostStale = null;
                for (Contact newContact : contacts) {
                    if (newContact.getStaleCount() >= 1) {
                        /* Contact is stale */
                        if (mostStale == null) {
                            mostStale = newContact;
                        } else if (newContact.getStaleCount() > mostStale.getStaleCount()) {
                            mostStale = newContact;
                        }
                    }
                }
                /* If we have a stale contact, remove it and add the new contact to the bucket */
                if (mostStale != null) {
                    contacts.remove(mostStale);
                    contacts.add(contact);
                } else {
                    /* No stale contact available to replcae, insert this node into replacement cache */
                    insertIntoReplacementCache(contact);
                }
            } else {
                contacts.add(contact);
            }
        }
    }

    /**
     * Inserts a node into the bucket as a contact
     *
     * @param node The node to insert into the bucket
     */
    public synchronized void insert(Node node) {
        insert(new Contact(node));
    }

    /**
     * Removes a contact from the bucket only if the replacement cache has a contact to replace it with. Else increments the contacts stale count
     *
     * @param contact The contact to remove from the bucket
     * @param force If true, remove the contact immediately without checking for a replacement in the replacement cache
     * @return Returns false if the contact doesn't exist.
     */
    public synchronized boolean removeContact(Contact contact, boolean force) {
        if (force == true) {
            removeContactForce(contact.getNode());
        } else {

        /* If the contact does not exist, then we failed to remove it */
            if (!contacts.contains(contact)) {
                return false;
            }

        /* Contact exist, lets remove it only if our replacement cache has a replacement */
            if (!replacementCache.isEmpty()) {
            /* Replace the contact with one from the replacement cache */
                contacts.remove(contact);
                Contact replacement = replacementCache.first();
                contacts.add(replacement);
                replacementCache.remove(replacement);
            } else {
            /* There is no replacement, just increment the contact's stale count */
                getContact(contact.getNode()).incrementStaleCount();
            }
        }
        return true;
    }

    /**
     * Removes the contact containing the node from the bucket
     *
     * @param node The node of the contact to remove from the bucket
     * @return Returns false if the contact doesn't exist.
     */
    public synchronized boolean removeContact(Node node, boolean force) {
        return removeContact(new Contact(node), force);
    }

    /**
     * Removes a contact from the bucket without checking for a replacement in the replacement cache
     *
     * @param node The node of the contact to remove from the bucket
     * @return The contact removed
     */
    private synchronized Contact removeContactForce(Node node) {
        for (Contact contact : contacts) {
            if (contact.getNode().equals(node)) {
                contacts.remove(contact);
                return contact;
            }
        }

        /* We got here means this element does not exist */
        throw new NoSuchElementException("Node does not exist in the replacement cache. ");
    }

    /**
     * Inserts a contact into the replacement cache
     */
    private synchronized void insertIntoReplacementCache(Contact c) {
        if (replacementCache.contains(c)) {
            /* Update the last seen time if this contact is already in our replacement cache */
            Contact tmp = removeFromReplacementCache(c.getNode());
            tmp.setSeenNow();
            replacementCache.add(tmp);
        } else if (replacementCache.size() > config.getK()) {
            /* If the cache is filled, remove the least recently seen contact */
            replacementCache.remove(replacementCache.last());
            replacementCache.add(c);
        } else {
            replacementCache.add(c);
        }
    }

    /**
     * Removes a contact from the replacement cache
     * @param node The node within a contact to remove from the replacement cache
     * @return The contact removed from the replacement cache
     */
    private synchronized Contact removeFromReplacementCache(Node node) {
        for (Contact contact : replacementCache) {
            if (contact.getNode().equals(node)) {
                replacementCache.remove(contact);
                return contact;
            }
        }

        /* We got here means this element does not exist */
        throw new NoSuchElementException("Node does not exist in the replacement cache. ");
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder("Number of nodes in bucket with depth: ");
        sb.append(depth);
        sb.append(": ");
        sb.append(getNumberOfContacts());
        sb.append("\n Nodes: \n");
        for (Contact n : contacts) {
            sb.append("Node: ");
            sb.append(n.getNode().getNodeId().toString());
            sb.append(" (stale: ");
            sb.append(n.getStaleCount());
            sb.append(")");
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Returns a specific contact in the bucket based on the provided node
     */
    private synchronized Contact getContact(Node n) {
        for (Contact c : contacts) {
            if (c.getNode().equals(n)) {
                return c;
            }
        }

        /* This contact does not exist */
        throw new NoSuchElementException("The contact does not exist in the contacts list.");
    }

    /**
     * Returns the total number of contacts in the bucket
     * @return
     */
    public synchronized int getNumberOfContacts() {
        return contacts.size();
    }

    public synchronized int getDepth() {
        return depth;
    }

    /**
     * Returns a list of all contacts in the bucket
     */
    public synchronized List<Contact> getContacts() {
        final ArrayList<Contact> list = new ArrayList<>();

        /* If we have no contacts, return the blank arraylist */
        if (contacts.isEmpty()) {
            return list;
        }

        /* We have contacts, lets copy put them into the arraylist and return */
        for (Contact c : contacts) {
            list.add(c);
        }

        return list;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }
}
