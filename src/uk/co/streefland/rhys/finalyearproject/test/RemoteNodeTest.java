package uk.co.streefland.rhys.finalyearproject.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.exceptions.BootstrapException;
import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.storage.StorageHandler;

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

        Configuration config = new Configuration();
        StorageHandler storageHandler = new StorageHandler(config);

        Node destination;
        String localIp;
        String remoteIp;

        String message;

        try {
            if (storageHandler.doesSavedStateExist() == false) {
                System.out.println("Please enter the local IP of the local machine");
                localIp = sc.nextLine();
            } else {
                localIp = null;
            }

            LocalNode localNode = new LocalNode(localIp);

            if (args.length > 0) {
                if (args[0].equals("-first")) {
                    System.out.println("This is the first node in the network");
                }
            } else {
                if (storageHandler.doesSavedStateExist() == false) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("What IP would you like to connect to?");
                    remoteIp = sc.nextLine();

                    destination = new Node(new KeyId(), InetAddress.getByName(remoteIp), 12345);

                    logger.info("Connecting to node at {} using port {}", remoteIp, 12345);
                    localNode.bootstrap(destination);
                }
            }

            logger.info("Printing routing table");
            System.out.println(localNode.getRoutingTable());

            do  {
                message = sc.nextLine();
                if (message != null) {
                    localNode.message(message, localNode.getRoutingTable().getAllNodes());
                }
            } while(!message.equals("exit"));

            localNode.shutdown();

        } catch (BootstrapException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
