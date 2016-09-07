package uk.co.streefland.rhys.finalyearproject.test;

import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Rhys on 08/07/2016.
 */
public class NodeTest {

    public static void main(String[] args) throws UnknownHostException {

        KeyId nodeId1 = new KeyId();
        KeyId nodeId2 = new KeyId();

        InetAddress inet1 = InetAddress.getByName("127.0.0.1");
        InetAddress inet2 = InetAddress.getByName("127.0.0.2");

        Node node1 = new Node(nodeId1, inet1, 8080);
        Node node2 = new Node(nodeId2, inet2 , 8081);

        System.out.println("NODE TESTING :)\n");

        System.out.println("NODE1 TOSTRING: " + node1.toString());
        System.out.println("NODE2 TOSTRING: " + node2.toString());

        System.out.println("\nDoes node1 equal node 2: " + node1.equals(node2));
    }
}
