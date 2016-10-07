package uk.co.streefland.rhys.finalyearproject.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.storage.StorageHandler;

import java.io.IOException;
import java.util.Scanner;

/**
 * Connect some nodes on the local machine together
 */
public class LocalNodeTest {

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(LocalNodeTest.class);

        Configuration config = new Configuration();
        StorageHandler storageHandler = new StorageHandler(config);

        Scanner sc = new Scanner(System.in);
        String ip;
        String message;

        try {

            if (storageHandler.doesSavedStateExist() == false) {
                System.out.println("Please enter the local IP of the local machine");
                ip = sc.nextLine();
            } else {
                ip = null;
            }

            LocalNode localNode = new LocalNode(ip);

            do  {
                message = sc.nextLine();
                if (message != null) {
                    localNode.broadcastMessage(message, localNode.getRoutingTable().getAllNodes());
                }
            } while(!message.equals("exit"));

            localNode.shutdown();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
