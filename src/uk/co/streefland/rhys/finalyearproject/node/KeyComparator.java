package uk.co.streefland.rhys.finalyearproject.node;

import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;

import java.math.BigInteger;
import java.util.Comparator;

/**
 * A Comparator to compare 2 keys to a given key
 */
public class KeyComparator implements Comparator<Node> {

    private final BigInteger key;

    /**
     * @param key The NodeId relative to which the distance should be measured.
     */
    public KeyComparator(NodeId key) {
        this.key = key.getInt();
    }

    /**
     * Compare two objects and determine which is closest to the NodeId specified in the
     * constructor.
     *
     * @param n1 Node 1 to compare distance from the key
     * @param n2 Node 2 to compare distance from the key
     */
    @Override
    public int compare(Node n1, Node n2) {
        BigInteger b1 = n1.getNodeId().getInt();
        BigInteger b2 = n2.getNodeId().getInt();

        b1 = b1.xor(key);
        b2 = b2.xor(key);

        return b1.abs().compareTo(b2.abs());
    }
}
