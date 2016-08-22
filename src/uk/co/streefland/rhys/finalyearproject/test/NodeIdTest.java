package uk.co.streefland.rhys.finalyearproject.test;

import uk.co.streefland.rhys.finalyearproject.node.NodeId;

/**
 * Created by Rhys on 08/07/2016.
 */
public class NodeIdTest {

    public static void main(String[] args) {
        System.out.println("NODE ID TESTING :)");

        NodeId nodeId = new NodeId();

        System.out.println("\nBytes: " + nodeId.getIdBytes().toString());

        System.out.println("\nHashcode: " + nodeId.hashCode());

        System.out.println("\nHex Representation: " + nodeId.toString());

        System.out.println("\nBinary Representation: " + nodeId.toBinary());

        System.out.println("\nIndex of first set bit: " + nodeId.getFirstSetBitIndex());

    }
}
