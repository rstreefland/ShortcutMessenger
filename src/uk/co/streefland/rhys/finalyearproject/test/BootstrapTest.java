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
            LocalNode node1 = new LocalNode("rhys", new NodeId("ASF45678947584567467"), 7574);
            LocalNode node2 = new LocalNode("gareth", new NodeId("ASERTKJDHGVHERJHGFLK"), 7572);
            LocalNode node3 = new LocalNode("peter", new NodeId("L7ER1KJ7HGVHERJHGFLA"), 7573);

            node2.bootstrap(node1.getNode());

            node3.bootstrap(node2.getNode());

            System.out.println("here1");

            System.out.println(node3.getRoutingTable());

            System.out.println("here2");


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
