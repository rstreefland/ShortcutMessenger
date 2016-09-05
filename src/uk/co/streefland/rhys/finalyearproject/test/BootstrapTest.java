package uk.co.streefland.rhys.finalyearproject.test;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;
import uk.co.streefland.rhys.finalyearproject.routing.RoutingTable;

import java.io.IOException;

/**
 * Created by Rhys on 30/08/2016.
 */
public class BootstrapTest {
    public static void main(String[] args) {

        try {
            LocalNode node1 = new LocalNode(new NodeId("ASF45678947584567467"), 7574);
            LocalNode node2 = new LocalNode(new NodeId(), 7572);
            LocalNode node3 = new LocalNode(new NodeId("L7ER1KJ7HGVHERJHGFLA"), 7573);
            LocalNode node4 = new LocalNode(new NodeId("P7ED1KJ7HGVHERJHGFLA"), 7575);
            LocalNode node5 = new LocalNode(new NodeId(), 7576);

            node2.bootstrap(node1.getNode());

            node3.bootstrap(node1.getNode());

            node4.bootstrap(node1.getNode());

            node5.bootstrap(node1.getNode());

            System.out.println(node1.getRoutingTable());
            System.out.println(node5.getRoutingTable());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
