package uk.co.streefland.rhys.finalyearproject.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;

import java.io.IOException;

/**
 * Connect some nodes on the local machine together
 */
public class LocalNodesTest {

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(LocalNodesTest.class);

        try {
            long startTime = System.currentTimeMillis();

            LocalNode node1 = new LocalNode(new NodeId(), 8001);
            LocalNode node2 = new LocalNode(new NodeId(), 8002);

            logger.info("Connecting node1 and node2");
            node1.bootstrap(node2.getNode());

            LocalNode node3 = new LocalNode(new NodeId(), 8003);

            logger.info("Connecting node3 and node 2");
            node3.bootstrap(node2.getNode());

            LocalNode node4 = new LocalNode(new NodeId(), 8004);

            logger.info("Connecting node4 and node 2");
            node4.bootstrap(node2.getNode());

            System.out.println(node1.getRoutingTable());
            System.out.println(node2.getRoutingTable());
            System.out.println(node3.getRoutingTable());
            System.out.println(node4.getRoutingTable());

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime);
            System.out.println("DURATION: " + duration + "ms");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
