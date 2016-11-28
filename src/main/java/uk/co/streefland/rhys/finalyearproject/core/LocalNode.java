package uk.co.streefland.rhys.finalyearproject.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.message.MessageHandler;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.operation.ConnectOperation;
import uk.co.streefland.rhys.finalyearproject.operation.refresh.RefreshHandler;
import uk.co.streefland.rhys.finalyearproject.routing.RoutingTable;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;

/**
 * Represents the local node on the network. This class ties together all of the functionality of the other classes ---
 */
public class LocalNode implements Runnable {

    private static final String BUILD_NUMBER = "544";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /* DHT objects */
    private IPTools ipTools;
    private Configuration config;
    private Node localNode;
    private Server server;
    private RoutingTable routingTable;
    private StorageHandler storageHandler;
    private MessageHandler messageHandler;
    private Users users;
    private Messages messages;

    /* Objects for refresh operation */
    private Timer refreshOperationTimer;
    private RefreshHandler refreshHandler;

    /**
     * This constructor is the main constructor and attempts to read the localNode and routingTable objects from a file.
     * It creates new objects if they cannot be loaded from the file.
     *
     * @throws IOException
     */
    public LocalNode(IPTools ipTools, InetAddress publicIp, InetAddress privateIp, int port) throws IOException{
        logger.info("FinalYearProject build {}", BUILD_NUMBER);

        this.ipTools = ipTools;

        /* Read config, localNode, routingTable and users from file if possible; else create new objects */
        this.storageHandler = new StorageHandler();
        readState(publicIp, privateIp, port);

        this.messageHandler = new MessageHandler(this);

        boolean portBindFailure;

        do {
            try {
                portBindFailure = false;
                this.server = new Server(port, messageHandler, config, localNode);
            } catch (IOException e) {
                portBindFailure = true;
                logger.warn("Couldn't bind to port " + port);
                port++;
                logger.warn("Using port " + port + " instead");

                localNode.setPrivatePort(port);
                localNode.setPublicPort(port);
            }
        } while (portBindFailure);

        /* Couldn't read it from file */
        if (users == null) {
            users = new Users(this);
        } else {
            users.initialise(this);
        }

        /* If we've managed to load a saved state from file - start the server */
        if (!routingTable.isEmpty()) {
            server.startListener();
            /* Start the automatic refresh operation that runs every 60 seconds */
            startRefreshOperation();
        }
        logger.info("LocalNode ID:" + localNode.getNodeId());
    }

    /**
     * This constructor exists for tests that create multiple nodes with different ports on the local machine.
     * It doesn't load any existing configuration from a file
     *
     * @param defaultId The nodeId of the localNode
     * @param port      The port the server should listen on
     * @throws IOException
     */
    public LocalNode(KeyId defaultId, int port) throws IOException {
        logger.info("FinalYearProject build {}", BUILD_NUMBER);

        this.ipTools = new IPTools();

        this.localNode = new Node(defaultId, InetAddress.getLocalHost(), InetAddress.getLocalHost(), port, port);
        this.config = new Configuration();
        this.storageHandler = new StorageHandler();
        this.routingTable = new RoutingTable(localNode);

        this.messageHandler = new MessageHandler(this);
        this.server = new Server(port, messageHandler, config, localNode);
        this.users = new Users(this);

        server.startListener();
        startRefreshOperation();
    }

    @Override
    public void run() {

    }

    /**
     * If a saved state file exists then it will read the localNode and routingTable objects from that file.
     * If it cannot read these objects from the file it will create new objects
     *
     * @throws IOException
     */
    private void readState(InetAddress publicIp, InetAddress privateIp, int port) throws IOException {
        if (storageHandler.doesSavedStateExist()) {
            logger.info("Saved state found - attempting to read ");

            /* Read objects from file */
            storageHandler.load();

            /* Get config object from storageHandler */
            Configuration newConfig = storageHandler.getConfig();

            if (newConfig != null) {
                logger.info("Config read successfully");
                config = newConfig;
            } else {
                logger.warn("Failed to read config from saved state - defaulting to new config");
                config = new Configuration();
                if (port != 0) {
                    config.setPort(port);
                }
            }

            /* Get localNode object from storageHandler */
            Node newLocalNode = storageHandler.getLocalNode();

            if (newLocalNode != null) {
                logger.info("Local node read successfully");
                localNode = newLocalNode;
            } else {
                logger.warn("Failed to read local node from saved state - defaulting to creating a new local node");
                localNode = new Node(new KeyId(), publicIp, privateIp, config.getPort(), config.getPort());
            }

            /* Get routingTable object from storageHandler */
            RoutingTable newRoutingTable = storageHandler.getRoutingTable();

            if (newRoutingTable != null) {
                logger.info("Routing table read successfully");
                routingTable = newRoutingTable;
            } else {
                logger.warn("Failed to read routing table from saved state - defaulting to creating a new routing table");
                routingTable = new RoutingTable(localNode);
            }

            /* Get users object from storageHandler */
            Users newUsers = storageHandler.getUsers();

            if (newUsers != null) {
                logger.info("Users read successfully");
                users = newUsers;
            } else {
                logger.warn("Failed to read users from saved state - defaulting to creating a new users object");
            }

            /* Get users object from storageHandler */
            Messages newMessages = storageHandler.getMessages();

            if (newMessages != null) {
                logger.info("Messages read successfully");
                messages = newMessages;
                messages.init(this);
            } else {
                logger.warn("Failed to read messages from saved state - defaulting to creating a new messages object");
                messages = new Messages(this);
            }

        } else {
            logger.info("Saved state not found");
            config = new Configuration();
            if (port != 0) {
                config.setPort(port);
            }
            localNode = new Node(new KeyId(), publicIp, privateIp, config.getPort(), config.getPort());
            routingTable = new RoutingTable(localNode);
            messages = new Messages(this);
        }
    }

    /**
     * Saves the localNode and routingTable objects to a file using the StorageHandler class
     */
    private void saveState() {
        logger.info("Saving state to file");
        storageHandler.save(config, localNode, routingTable, users, messages);
    }

    /**
     * Starts the automatic refresh operation
     */
    private void startRefreshOperation() {
        /* Create the refresh operation */
        refreshHandler = new RefreshHandler(this);

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

        return connect.isError();
    }

    public final void message(String message, User userToMessage) throws IOException {
            messages.sendMessage(message, userToMessage);
    }

    /**
     * Shuts down the server cleanly
     */
    public void shutdown(boolean saveState) {
        server.shutdown();  // Shut down the listener
        stopRefreshOperation(); // Stop the automatic refresh timer

        if (saveState) {
            saveState(); // Save the localNode and routingTable objects to file
        }

        logger.info("Server shut down successfully");
    }

    public IPTools getIpTools() {
        return ipTools;
    }

    public Configuration getConfig() {
        return config;
    }

    public Node getNode() {
        return localNode;
    }

    public void setNode(Node node) {
        this.localNode = node;
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

    public Messages getMessages() {
        return messages;
    }
}
