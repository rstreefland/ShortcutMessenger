package uk.co.streefland.rhys.finalyearproject.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.storage.StorageHandler;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Rhys on 05/09/2016.
 */
class FileTest {
    // THIS CLASS NO LONGER WORKS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(FileTest.class);

        Node node = null;

        Configuration config = new Configuration();
        StorageHandler storageHandler = new StorageHandler();

        if (storageHandler.doesSavedStateExist()) {
            logger.info("Reading existing configuration...");
            storageHandler.load();
            node = storageHandler.getLocalNode();
        } else {
            logger.info("No existing configuration found - creating a new node");
            try {
                node = new Node(new KeyId(), InetAddress.getByName("192.168.0.10"), 1234);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        logger.info("KeyId is {}", node.getNodeId());

        //storageHandler.save();

        logger.info("Node written to file");
    }
}
