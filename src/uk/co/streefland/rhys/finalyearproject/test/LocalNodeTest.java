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

        Scanner sc = new Scanner(System.in);
        String message;

        try {

            System.out.println("Please enter the IP of the local machine");
            String ip = sc.nextLine();

            LocalNode localNode = new LocalNode(ip);

            do  {
                message = sc.nextLine();
                if (message != null) {
                    localNode.message(message, localNode.getRoutingTable().getAllNodes());
                }
            } while(!message.equals("exit"));

            localNode.shutdown();

        } catch (IOException e) {
            System.out.println("I caught an exception here - you'll want to move it");
            e.printStackTrace();
        }
    }
}
