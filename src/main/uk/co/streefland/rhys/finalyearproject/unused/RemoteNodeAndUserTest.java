package uk.co.streefland.rhys.finalyearproject.unused;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.core.StorageHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Connect some nodes on the local machine together
 */
class RemoteNodeAndUserTest {

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(RemoteNodeAndUserTest.class);
        Scanner sc = new Scanner(System.in);

        Configuration config = new Configuration();
        StorageHandler storageHandler = new StorageHandler();

        Node destination;
        String localIp;
        String remoteIp;

        String username;
        String password;

        try {
            if (!storageHandler.doesSavedStateExist()) {
                System.out.println("Please enter the local IP of the local machine");
                localIp = sc.nextLine();
            } else {
                localIp = null;
            }

            LocalNode localNode = new LocalNode(localIp, 0);

            if (args.length > 0) {
                if (args[0].equals("-first")) {
                    System.out.println("This is the first node in the network");
                }
            } else {
                if (!storageHandler.doesSavedStateExist()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("What IP would you like to node to?");
                    remoteIp = sc.nextLine();

                    destination = new Node(new KeyId(), InetAddress.getByName(remoteIp), 12345);

                    logger.info("Connecting to node at {} using port {}", remoteIp, 12345);
                    localNode.bootstrap(destination);
                }
            }

            logger.info("Printing routing table");
            System.out.println(localNode.getRoutingTable());

            do {
                System.out.println("Please enter a username:");
                username = sc.nextLine();

                System.out.println("Please enter a password:");
                password = sc.nextLine();

                User user = new User(username, password);

                if (localNode.getUsers().registerUser(user)) {
                    logger.error("USER ALREADY EXISTS");
                }

            } while (!password.equals("exit"));

            localNode.shutdown();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}