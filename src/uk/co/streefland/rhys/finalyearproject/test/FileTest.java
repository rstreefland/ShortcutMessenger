package uk.co.streefland.rhys.finalyearproject.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;
import uk.co.streefland.rhys.finalyearproject.storage.StorageHandler;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Rhys on 05/09/2016.
 */
public class FileTest {
// THIS CLASS NO LONGER WORKS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public static void main(String []args) {

        Logger logger = LoggerFactory.getLogger(FileTest.class);

        Node node = null;

        Configuration config = new Configuration();
        StorageHandler storageHandler = new StorageHandler(config);

        if (storageHandler.doesSavedStateExist()) {
            logger.info("Reading existing configuration...");
            storageHandler.load();
            node = storageHandler.getLocalNode();
        } else {
            logger.info("No existing configuration found - creating a new node");
            try {
                node = new Node(new NodeId(), InetAddress.getByName("192.168.0.10"), 1234);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        logger.info("NodeId is {}", node.getNodeId());

        //storageHandler.save();

        logger.info("Node written to file");
    }
}
