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
        String ip;
        int port;

        String message;

        try {
            LocalNode localNode = new LocalNode();

            System.out.println("What IP would you like to connect to?");
            ip = sc.nextLine();

            System.out.println("What port would you like to connect to?");
            port = sc.nextInt();

            inetAddress = InetAddress.getByName(ip);

            destination = new Node(new NodeId(), inetAddress, port);

            logger.info("Connecting to node at {} using port {}", ip, port);
            localNode.bootstrap(destination);

            logger.info("Printing routing table");
            System.out.println(localNode.getRoutingTable());

            while(true) {

                message = sc.nextLine();
                if (message != null) {
                    localNode.message(message, localNode.getRoutingTable().getAllNodes());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
