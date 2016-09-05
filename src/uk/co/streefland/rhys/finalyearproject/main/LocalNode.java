package uk.co.streefland.rhys.finalyearproject.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.message.MessageHandler;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;
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

    /* for prod - loads from file if possible */
    public LocalNode() throws IOException {
        this.config = new Configuration();
        this.storageHandler = new StorageHandler(config);

        readExistingState();

        this.messageHandler = new MessageHandler(this, config);
        this.server = new Server(config.getPort(), messageHandler, localNode, config);

        startRefreshOperation();
    }

    /* for testing - doesn't load from file */
    public LocalNode(NodeId defaultId, int port) throws IOException {
        this.localNode = new Node(defaultId, InetAddress.getLocalHost(), port);
        this.config = new Configuration();
        this.storageHandler = new StorageHandler(config);
        this.routingTable = new RoutingTable(localNode, config);

        this.messageHandler = new MessageHandler(this, config);
        this.server = new Server(port, messageHandler, localNode, config);

        startRefreshOperation();
    }

    private void readExistingState() throws IOException {
        if (storageHandler.doesSavedStateExist() == true) {
            logger.info("Saved state found - attempting to read ");

            storageHandler.load();

            Node newLocalNode = storageHandler.getLocalNode();

            if (newLocalNode != null) {
                logger.info("Local node read successfully");
                localNode = newLocalNode;
            } else {
                logger.warn("Failed to read local node from saved state - defaulting to creating a new local node");
                localNode = new Node(new NodeId(), InetAddress.getLocalHost(), config.getPort());
            }

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
            localNode = new Node(new NodeId(), InetAddress.getLocalHost(), config.getPort());
            routingTable = new RoutingTable(localNode, config);
        }
    }

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

    public synchronized final void message(String message, List<Node> targetNodes) throws IOException {
        if (!message.isEmpty() || message.equals("exit")) { // TODO: 05/09/2016  remove the exit check once you have some kind of a user interface
            logger.info("Sending message to specified nodes");
            Operation sendMessage = new TextMessageOperation(server, this, config, message, targetNodes);
            sendMessage.execute();
        }
    }

    /**
     * Shuts down the server cleanly
     */
    public void shutdown() {
        /* Shut down the server */
        server.shutdown();
        stopRefreshOperation();
        saveState();
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
