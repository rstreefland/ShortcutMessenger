package uk.co.streefland.rhys.finalyearproject.test;

import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.StorageHandler;

import java.io.IOException;
import java.util.Scanner;

/**
 * Connect some nodes on the local machine together
 */
class LocalNodeTest {

    public static void main(String[] args) {

        Configuration config = new Configuration();
        StorageHandler storageHandler = new StorageHandler();

        Scanner sc = new Scanner(System.in);
        String ip;
        String message;

        try {

            if (!storageHandler.doesSavedStateExist()) {
                System.out.println("Please enter the local IP of the local machine");
                ip = sc.nextLine();
            } else {
                ip = null;
            }

            LocalNode localNode = new LocalNode(ip, 0);

            do {
                message = sc.nextLine();
                if (message != null) {
                    localNode.broadcastMessage(message, localNode.getRoutingTable().getAllNodes());
                }
            } while (!message.equals("exit"));

            localNode.shutdown();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
