package uk.co.streefland.rhys.finalyearproject.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.message.MessageHandler;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.operation.*;
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

    public static final String BUILD_NUMBER = "191";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Configuration config;
    private Node localNode;
    private RoutingTable routingTable;
    private final Server server;
    private final StorageHandler storageHandler;
    private Users users;

    /* Objects for refresh operation */
    private Timer refreshOperationTimer;
    private RefreshHandler refreshHandler;

    /* MessageHandler object to create and receive messages */
    private final MessageHandler messageHandler;

    /**
     * This constructor is the core constructor and attempts to read the localNode and routingTable objects from a file.
     * It creates new objects if they cannot be loaded from the file.
     *
     * @throws IOException
     */
    public LocalNode(String localIp) throws IOException {
        logger.info("FinalYearProject build " + BUILD_NUMBER);

        this.config = new Configuration();
        this.storageHandler = new StorageHandler(config);

        /* Read localNode, routingTable and users from file if possible; else create new objects */
        readState(localIp);

        this.messageHandler = new MessageHandler(this, config);
        this.server = new Server(config.getPort(), messageHandler, localNode, config);

        /* Couldn't read it from file */
        if (users == null) {
            users = new Users(server, this, config);
        }

        /* If we've managed to load a saved state from file - start the server */
        if (routingTable.getAllNodes().size() > 1) {
            server.startListener();
            /* Start the automatic refresh operation that runs every 60 seconds */
            startRefreshOperation();
        }
    }

    /**
     * This constructor is the same the above but allows the specification of a custom port
     *
     * @throws IOException
     */
    public LocalNode(String localIp, int port) throws IOException {
        logger.info("FinalYearProject build " + BUILD_NUMBER);

        this.config = new Configuration();
        config.setPort(port);

        this.storageHandler = new StorageHandler(config);

        /* Read localNode, routingTable and users from file if possible; else create new objects */
        readState(localIp);

        this.messageHandler = new MessageHandler(this, config);
        this.server = new Server(config.getPort(), messageHandler, localNode, config);

        /* Couldn't read it from file */
        if (users == null) {
            users = new Users(server, this, config);
        }

        /* If we've managed to load a saved state from file - start the server */
        if (routingTable.getAllNodes().size() > 1) {
            server.startListener();
            /* Start the automatic refresh operation that runs every 60 seconds */
            startRefreshOperation();
        }
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
        logger.info("FinalYearProject build " + BUILD_NUMBER);

        this.localNode = new Node(defaultId, InetAddress.getLocalHost(), port);
        this.config = new Configuration();
        this.storageHandler = new StorageHandler(config);
        this.routingTable = new RoutingTable(localNode, config);

        this.messageHandler = new MessageHandler(this, config);
        this.server = new Server(port, messageHandler, localNode, config);
        this.users = new Users(server, this, config);
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

            /* Get users object from storageHandler */
            Users newUsers = storageHandler.getUsers();

            if (newUsers != null) {
                logger.info("Users read successfully");
                users = newUsers;
                users.updateAfterLoad(server, this, config);
            } else {
                logger.warn("Failed to read users from saved state - defaulting to creating a new users object");
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
        logger.info("Saving state to file");
        storageHandler.save(localNode, routingTable, users);
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
     * This method starts the server if it isn't already running.
     * It is designed for the first node on the network
     */
    public final void first() {
        /* If server is not running then start it */
        if (!server.isRunning()) {
            server.startListener(); // start the server
            startRefreshOperation(); // start the automatic refresh operation that runs every 60 second
        }
    }

    /**
     * Begins the process of bootstrapping to the network
     *
     * @param node The target node
     * @throws IOException
     */
    public final boolean bootstrap(Node node) throws IOException {

        /* If server is not running then start it */
        if (!server.isRunning()) {
            server.startListener(); // start the server
            startRefreshOperation(); // start the automatic refresh operation that runs every 60 second
        }

        logger.info("Bootstrapping localnode {} to node {}", localNode.toString(), node.toString());
        ConnectOperation connect = new ConnectOperation(server, this, node, config);
        connect.execute();

        /* Bring localNode up to date with data from the DHT */
        refreshHandler.run();

        return connect.isError();
    }

    /**
     * Sends a broadcast message to the specified nodes
     * @param message The text message to broadcast
     * @param targetNodes The nodes that should receive the message
     * @throws IOException
     */
    public final void broadcastMessage(String message, List<Node> targetNodes) throws IOException {
        if (!message.isEmpty()) {
            logger.info("Sending message to specified nodes");
            Operation operation = new BroadcastMessageOperation(server, this, config, message, targetNodes);
            operation.execute();
        }
    }

    public final void message(String message, User userToMessage) throws IOException {
        if (!message.isEmpty()) {
            logger.info("Sending message to " + userToMessage);
            SendMessageOperation operation = new SendMessageOperation(server, this, config, userToMessage, message);
            operation.execute();
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

    public Users getUsers() {
        return users;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public Server getServer() {
        return server;
    }
}
