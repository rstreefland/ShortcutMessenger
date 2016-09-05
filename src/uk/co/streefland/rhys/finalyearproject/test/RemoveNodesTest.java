package uk.co.streefland.rhys.finalyearproject.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Connect some nodes on the local machine together
 */
public class RemoveNodesTest {

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(RemoveNodesTest.class);

        try {
            LocalNode localNode = new LocalNode();

            Node node1 = new Node(new NodeId(), InetAddress.getByName("127.0.0.1"), 1234);
            Node node2 = new Node(new NodeId(), InetAddress.getByName("127.0.0.1"), 1234);
            Node node3 = new Node(new NodeId(), InetAddress.getByName("127.0.0.1"), 1234);
            Node node4 = new Node(new NodeId(), InetAddress.getByName("127.0.0.1"), 1234);

            localNode.getRoutingTable().insert(node1);

            System.out.println(localNode.getRoutingTable().toString());

            localNode.getRoutingTable().insert(node2);

            System.out.println(localNode.getRoutingTable().toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
