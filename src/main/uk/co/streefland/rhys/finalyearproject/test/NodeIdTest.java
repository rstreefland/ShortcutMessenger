package uk.co.streefland.rhys.finalyearproject.test;

import uk.co.streefland.rhys.finalyearproject.node.KeyId;

import java.util.Arrays;

/**
 * Created by Rhys on 08/07/2016.
 */
class NodeIdTest {

    public static void main(String[] args) {
        System.out.println("NODE ID TESTING :)");

        KeyId nodeId = new KeyId("titanicsaled");
        KeyId nodeId2 = nodeId.generateKeyIdUsingDistance(160);

        System.out.println("\nBytes: " + Arrays.toString(nodeId.getIdBytes()));

        System.out.println("\nHashcode: " + nodeId.hashCode());

        System.out.println("\nString Representation: " + nodeId.toString());

        System.out.println("\nBinary Representation: " + nodeId.toBinary());

        System.out.println("\nIndex of first set bit: " + nodeId.getFirstSetBitLocation());

        System.out.println("Binary Representation: " + nodeId2.toBinary());

        System.out.println("DistanceNEW = " + nodeId.getDistance(nodeId2));
    }
}
