package uk.co.streefland.rhys.finalyearproject.routing;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares contacts by their last seen time
 */
public class LastSeenComparator implements Comparator<Contact>, Serializable
{

    /**
     * Compare two contacts to determine their order in the Bucket,
     * Contacts are ordered by their last seen timestamp.
     *
     * @param c1 Contact 1
     * @param c2 Contact 2
     */
    @Override
    public int compare(Contact c1, Contact c2)
    {
        if (c1.getNode().equals(c2.getNode()))
        {
            return 0;
        }
        else
        {
            /* We may have 2 different contacts with same last seen values so we can't return 0 here */
            return c1.getLastSeen() > c2.getLastSeen() ? 1 : -1;
        }
    }
}