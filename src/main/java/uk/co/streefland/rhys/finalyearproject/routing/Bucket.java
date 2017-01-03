package uk.co.streefland.rhys.finalyearproject.routing;

import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * A bucket in the Routing Table
 */
public class Bucket implements Serializable {

    /* Depth of the bucket in the RoutingTable */
    private final int depth;

    /* Contacts stored in this bucket */
    private final TreeSet<Contact> contacts;

    /* A set of recently seen contacts that can replace any contact that is unresponsive in the main set */
    private TreeSet<Contact> cache;

    public Bucket(int depth) {
        this.depth = depth;
        this.contacts = new TreeSet<>();
        this.cache = new TreeSet<>();
    }

    /**
     * Inserts a new contact into the bucket. If the bucket is full and there are no stale contacts to replace, the new contact will be inserted into the replacement cache
     *
     * @param c The contact to insert into the bucket
     */
    public synchronized void insert(Contact c) {

        if (contacts.contains(c)) {
            Contact temp = c;
            removeContact(c.getNode(), true); /* Remove from the TreeSet */
            temp.setSeenNow();    /* Update the last seen time*/
            temp.resetStaleCount();   /* Reset the stale count */
            contacts.add(temp); /* Re-add to the TreeSet so the set is sorted correctly */
        } else {
            /* If the bucket is full, put the contact into the cache instead */
            if (contacts.size() >= Configuration.K) {
                Contact mostStale = null;
                for (Contact newContact : contacts) {
                    if (newContact.getStaleCount() >= 1) {
                        /* Contact is stale - update the mostStale contact object */
                        if (mostStale == null) {
                            mostStale = newContact;
                        } else if (newContact.getStaleCount() > mostStale.getStaleCount()) {
                            mostStale = newContact;
                        }
                    }
                }
                /* If there is a stale contact, remove it and add the new contact to the bucket */
                if (mostStale != null) {
                    contacts.remove(mostStale);
                    contacts.add(c);
                } else {
                    /* No stale contact available to replace, insert new contact into the cache */
                    insertIntoCache(c);
                }
            } else {
                contacts.add(c);
            }
        }
    }

    /**
     * Inserts a node into the bucket as a contact
     *
     * @param n The node to insert into the bucket
     */
    public synchronized void insert(Node n) {
        insert(new Contact(n));
    }

    /**
     * Inserts a contact into the cache
     */
    private synchronized void insertIntoCache(Contact c) {
        if (cache.contains(c)) {
            /* Update the last seen time if this contact is already in the cache */
            Contact temp = c;
            cache.remove(c.getNode());
            temp.setSeenNow();
            cache.add(temp);
        } else if (cache.size() > Configuration.K) {
            /* If the cache is filled, remove the least recently seen contact */
            cache.remove(cache.last());
            cache.add(c);
        } else {
            cache.add(c);
        }
    }

    /**
     * Removes a contact from the bucket only if the cache has a contact to replace it with. Else increments the contacts stale count
     *
     * @param n  The node representing the contact to remove from the bucket
     * @param force If true, remove the contact immediately without checking for a replacement in the replacement cache
     * @return Returns true if the contact was removed
     */
    public synchronized boolean removeContact(Node n, boolean force) {

        Contact contact = getContact(n);

        if (contact == null) {
            return false;
        }

        if (force) {
            contacts.remove(contact);
            cache.remove(contact);
            return true;
        } else {
            /* Contact exist, remove it only if there's a replacement available in the cache */
            if (!cache.isEmpty()) {
                /* Replace the contact with one from the cache */
                contacts.remove(contact);
                Contact replacement = cache.first();
                contacts.add(replacement);
                cache.remove(replacement);
                return true;
            } else {
                /* No available replacement, increment the stale count instead */
                contact.incrementStaleCount();
            }
        }
        return false;
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
        return null;
    }

    /**
     * Returns a list of all contacts in the bucket
     */
    public synchronized List<Contact> getContacts() {
        ArrayList<Contact> list = new ArrayList<>();

        /* If we have no contacts, return the blank ArrayList */
        if (contacts.isEmpty()) {
            return list;
        }

        /* We have contacts, put them into the ArrayList and return */
        for (Contact c : contacts) {
            list.add(c);
        }

        /* Do the same for the cache */
        for (Contact c : cache) {
            list.add(c);
        }

        return list;
    }

    /**
     * Returns the total number of contacts in the bucket
     *
     * @return
     */
    public synchronized int getNumberOfContacts() {
        return contacts.size();
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
}
