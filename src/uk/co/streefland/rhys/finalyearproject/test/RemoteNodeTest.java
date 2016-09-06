package uk.co.streefland.rhys.finalyearproject.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Connect some nodes on the local machine together
 */
public class RemoteNodeTest {

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(RemoteNodeTest.class);
        Scanner sc = new Scanner(System.in);

        Node destination;
        InetAddress inetAddress;
        String localIp;
        String remoteIp;

        String message;

        try {
            System.out.println("Please enter the IP of the local machine");
            localIp = sc.nextLine();

            LocalNode localNode = new LocalNode(localIp);

            System.out.println("What IP would you like to connect to?");
            remoteIp = sc.nextLine();

            destination = new Node(new NodeId(), InetAddress.getByName(remoteIp), 12345);

            logger.info("Connecting to node at {} using port {}", remoteIp, 12345);
            localNode.bootstrap(destination);

            logger.info("Printing routing table");
            System.out.println(localNode.getRoutingTable());

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
