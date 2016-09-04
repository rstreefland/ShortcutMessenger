package uk.co.streefland.rhys.finalyearproject.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;

import java.io.IOException;
import java.util.Scanner;

/**
 * Connect some nodes on the local machine together
 */
public class LocalNodeTest {

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(LocalNodeTest.class);

        try {

            LocalNode localNode = new LocalNode("bleh1", new NodeId(), 1234);

            System.out.println(localNode.getRoutingTable());

            //node1.message(node1.getNode(), "bleh");

            String message;
            Scanner sc = new Scanner(System.in);

            while(true) {
                message = sc.nextLine();
                if (message != null) {
                    localNode.message(localNode.getNode(), message);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
