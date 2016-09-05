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

            LocalNode localNode = new LocalNode();

            String message;
            Scanner sc = new Scanner(System.in);

            do  {
                message = sc.nextLine();
                if (message != null) {
                    localNode.message(message, localNode.getRoutingTable().getAllNodes());
                }
            } while(!message.equals("exit"));

            localNode.shutdown();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
