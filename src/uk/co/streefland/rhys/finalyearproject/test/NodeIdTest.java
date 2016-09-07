package uk.co.streefland.rhys.finalyearproject.test;

import uk.co.streefland.rhys.finalyearproject.node.NodeId;

/**
 * Created by Rhys on 08/07/2016.
 */
public class NodeIdTest {

    public static void main(String[] args) {
        System.out.println("NODE ID TESTING :)");

        NodeId nodeId = new NodeId("titanicsaled");
        NodeId nodeId2 = nodeId.generateNodeIdUsingDistance(160);

        System.out.println("\nBytes: " + nodeId.getIdBytes().toString());

        System.out.println("\nHashcode: " + nodeId.hashCode());

        System.out.println("\nString Representation: " + nodeId.toString());

        System.out.println("\nBinary Representation: " + nodeId.toBinary());

        System.out.println("\nIndex of first set bit: " + nodeId.getFirstSetBitLocation());

        System.out.println("Binary Representation: " +nodeId2.toBinary());

        System.out.println("DistanceNEW = " +nodeId.getDistance(nodeId2));
    }
}
