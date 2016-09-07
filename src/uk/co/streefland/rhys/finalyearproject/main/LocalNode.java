package uk.co.streefland.rhys.finalyearproject.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.message.MessageHandler;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.operation.ConnectOperation;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;
import uk.co.streefland.rhys.finalyearproject.operation.RefreshHandler;
import uk.co.streefland.rhys.finalyearproject.operation.TextMessageOperation;
import uk.co.streefland.rhys.finalyearproject.routing.RoutingTable;
import uk.co.streefland.rhys.finalyearproject.storage.StorageHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Timer;

/**
 * Represents the local node on the network. This class ties together all of the functionality of the other classes
 */
public class LocalNode {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Configuration config;
    private Node localNode;
    private RoutingTable routingTable;
    private final Server server;
    private final StorageHandler storageHandler;

    /* Objects for refresh operation */
    private Timer refreshOperationTimer;
    private RefreshHandler refreshHandler;

    /* MessageHandler object to create and receive messages */
    private final MessageHandler messageHandler;

    /**
     * This constructor is the main constructor and attempts to read the localNode and routingTable objects from a file.
     * It creates new objects if they cannot be loaded from the file.
     *
     * @throws IOException
     */
    public LocalNode(String localIp) throws IOException {
        this.config = new Configuration();
        this.storageHandler = new StorageHandler(config);

        /* Read localNode and routingTable from file if possible; else create new objects */
        readState(localIp);

        this.messageHandler = new MessageHandler(this, config);
        this.server = new Server(config.getPort(), messageHandler, localNode, config);

        /* Start the automatic refresh operation that runs every 60 seconds */
        startRefreshOperation();
    }

    /**
     * This constructor exists for tests that create multiple nodes with different ports on the local machine.
     * It doesn't load any existing configuration from a file
     *
     * @param defaultId The nodeId of the localNode
     * @param port The port the server should listen on
     * @throws IOException
     */
    public LocalNode(KeyId defaultId, int port) throws IOException {
        this.localNode = new Node(defaultId, InetAddress.getLocalHost(), port);
        this.config = new Configuration();
        this.storageHandler = new StorageHandler(config);
        this.routingTable = new RoutingTable(localNode, config);

        this.messageHandler = new MessageHandler(this, config);
        this.server = new Server(port, messageHandler, localNode, config);

        /* Start the automatic refresh operation that runs every 60 seconds */
        startRefreshOperation();
    }

    /**
     * If a saved state file exists then it will read the localNode and routingTable objects from that file.
     * If it cannot read these objects from the file it will create new objects
     *
     * @throws IOException
     */
    private void readState(String localIp) throws IOException {
        if (storageHandler.doesSavedStateExist() == true) {
            logger.info("Saved state found - attempting to read ");

            /* Read objects from file */
            storageHandler.load();

            /* Get localNode object from storageHandler */
            Node newLocalNode = storageHandler.getLocalNode();

            if (newLocalNode != null) {
                logger.info("Local node read successfully");
                localNode = newLocalNode;
            } else {
                logger.warn("Failed to read local node from saved state - defaulting to creating a new local node");
                localNode = new Node(new KeyId(), InetAddress.getByName(localIp), config.getPort());
            }

            /* Get routingTable object from storageHandler */
            RoutingTable newRoutingTable = storageHandler.getRoutingTable();

            if (newRoutingTable != null) {
                logger.info("Routing table read successfully");
                routingTable = newRoutingTable;
                routingTable.updateConfigurationObjects(config);
            } else {
                logger.warn("Failed to read routing table from saved state - defaulting to creating a new routing table");
                routingTable = new RoutingTable(localNode, config);
            }
        } else {
            logger.info("Saved state not found");
            localNode = new Node(new KeyId(), InetAddress.getByName(localIp), config.getPort());
            routingTable = new RoutingTable(localNode, config);
        }
    }

    /**
     * Saves the localNode and routingTable objects to a file using the StorageHandler class
     */
    private void saveState() {
        storageHandler.save(localNode, routingTable);
    }

    /**
     * Starts the automatic refresh operation
     */
    private void startRefreshOperation() {
        /* Create the refresh operation */
        refreshHandler = new RefreshHandler(server, this, config);

        /* Create the timer and schedule it using the interval defined in Configuration.java  */
        refreshOperationTimer = new Timer(true);
        refreshOperationTimer.schedule(refreshHandler, config.getRefreshInterval(), config.getRefreshInterval());
    }

    /**
     * Stops and cleans up the automatic refresh operation
     */
    private void stopRefreshOperation() {
        refreshHandler.cancel();
        refreshOperationTimer.cancel();
        refreshOperationTimer.purge();
    }

    /**
     * Begins the process of bootstrapping to the network
     *
     * @param node The target node
     * @throws IOException
     */
    public synchronized final void bootstrap(Node node) throws IOException {
        logger.info("Bootstrapping localnode {} to node {}", localNode.toString(), node.toString());
        Operation connect = new ConnectOperation(server, this, node, config);
        connect.execute();
    }

    /**
     * Sends a broadcast message to the specified nodes
     * @param message The text message to broadcast
     * @param targetNodes The nodes that should receive the message
     * @throws IOException
     */
    public synchronized final void message(String message, List<Node> targetNodes) throws IOException {
        if (!message.isEmpty() && !message.equals("exit")) { // TODO: 05/09/2016  remove the exit check once you have some kind of a user interface
            logger.info("Sending message to specified nodes");
            Operation sendMessage = new TextMessageOperation(server, this, config, message, targetNodes);
            sendMessage.execute();
        }
    }

    /**
     * Shuts down the server cleanly
     */
    public void shutdown() {

        server.shutdown();  // Shut down the listener
        stopRefreshOperation(); // Stop the automatic refresh timer
        saveState(); // Save the localNode and routingTable objects to file
        logger.info("Server shut down successfully");
    }

    public Configuration getConfig() {
        return config;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public LocalNode getLocalNode() {
        return this;
    }

    public Node getNode() {
        return localNode;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public Server getServer() {
        return server;
    }
}
