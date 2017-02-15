package uk.co.streefland.rhys.finalyearproject.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.message.MessageHandler;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.operation.ConnectOperation;
import uk.co.streefland.rhys.finalyearproject.operation.refresh.BucketRefreshOperation;
import uk.co.streefland.rhys.finalyearproject.operation.refresh.RefreshHandler;
import uk.co.streefland.rhys.finalyearproject.routing.Bucket;
import uk.co.streefland.rhys.finalyearproject.routing.RoutingTable;

import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;

/**
 * Represents the local node on the network. This class ties together all of the functionality of the other classes ---
 */
public class LocalNode {

    public static final String BUILD_NUMBER = "1626";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /* DHT objects */
    private KeyId networkId;
    private IPTools ipTools;
    private Configuration config;
    private Node node;
    private Server server;
    private RoutingTable routingTable;
    private StorageHandler storageHandler;
    private MessageHandler messageHandler;
    private Users users;
    private Messages messages;
    private Encryption encryption;

    /* Refresh operation objects */
    private Timer refreshOperationTimer;
    private RefreshHandler refreshHandler;

    /**
     * This constructor is the main constructor and attempts to read the node and routingTable objects from a file.
     * It creates new objects if they cannot be loaded from the file.
     *
     * @throws IOException
     */
    public LocalNode(IPTools ipTools, int localPort) throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        logger.info("Shortcut Messenger build {}", BUILD_NUMBER);

        this.ipTools = ipTools;
        this.storageHandler = new StorageHandler();

        boolean savedStateExists = storageHandler.doesSavedStateExist();

        if (savedStateExists) {
            /* Read config, node, routingTable and users from file if possible; else create new objects */
            readState();
        } else {
            config = new Configuration();
            node = new Node(new KeyId(), ipTools.getPublicInetAddress(), ipTools.getPrivateInetAddress(), localPort, localPort);
            routingTable = new RoutingTable(node);
            messages = new Messages(this);
        }

        this.messageHandler = new MessageHandler(this);

        /* If port is already in use bind to the next port */
        boolean portBindFailure;
        do {
            try {
                portBindFailure = false;
                this.server = new Server(this, node.getPrivatePort());
            } catch (IOException e) {
                portBindFailure = true;
                logger.warn("Couldn't bind to port " + node.getPrivatePort());
                node.setPrivatePort(node.getPrivatePort() + 1);
                logger.warn("Using port " + node.getPrivatePort() + " instead");

                node.setPublicPort(node.getPrivatePort());
            }
        } while (portBindFailure);

        /* Couldn't read it from file */
        if (users == null) {
            users = new Users(this);
        } else {
            users.init(this);
        }

        encryption = new Encryption();

        if (!encryption.areKeysPresent() && savedStateExists) {
            throw new IOException("Public or private key is missing");
        }

        /* If we've managed to load a saved state from file - start the server */
        if (savedStateExists) {
            server.startListener();
            /* Start the automatic refresh operation that runs every 60 seconds */
            startRefreshOperation();

            new BucketRefreshOperation(this); // TODO: 15/02/2017  check if this breaks anything
        }
        logger.info("LocalNode ID:" + node.getNodeId());
    }

    /**
     * This constructor exists for tests that create multiple nodes with different ports on the local machine.
     * It doesn't load any existing configuration from a file
     *
     * @param defaultId The nodeId of the node
     * @param port      The port the server should listen on
     * @throws IOException
     */
    public LocalNode(KeyId networkId, KeyId defaultId, int port) throws IOException {
        logger.info("Shortcut Messenger build {}", BUILD_NUMBER);

        this.networkId = networkId;
        this.ipTools = new IPTools();

        this.node = new Node(defaultId, InetAddress.getLocalHost(), InetAddress.getLocalHost(), port, port);
        this.config = new Configuration();
        this.storageHandler = new StorageHandler();
        this.routingTable = new RoutingTable(node);

        this.messageHandler = new MessageHandler(this);
        this.server = new Server(this, port);
        this.users = new Users(this);

        server.startListener();
        startRefreshOperation();
    }

    /**
     * If a saved state file exists then it will read the node and routingTable objects from that file.
     * If it cannot read these objects from the file it will create new objects
     *
     * @throws IOException
     */
    private void readState() throws IOException, ClassNotFoundException {
            logger.info("Saved state found - attempting to read");

            /* Read objects from file */
            storageHandler.load();

            /* Get config object from storageHandler */
            Configuration newConfig = storageHandler.getConfig();

            if (newConfig != null) {
                logger.info("Config read successfully");
                config = newConfig;
            } else {
                throw new IOException("Failed to read config object from file");
            }

            /* Get networkId object from storageHandler */
            KeyId newNetworkId = storageHandler.getNetworkId();

            if (newNetworkId != null) {
                logger.info("NetworkId read successfully");
                networkId = newNetworkId;
            } else {
                throw new IOException("Failed to read networkId from file");
            }

            /* Get node object from storageHandler */
            Node newNode = storageHandler.getNode();

            if (newNode != null) {
                logger.info("Local node read successfully");
                /* IPs may have changed since the node was shut down - update the node object */
                newNode.setPublicInetAddress(ipTools.getPublicInetAddress());
                newNode.setPrivateInetAddress(ipTools.getPrivateInetAddress());
                node = newNode;
            } else {
                throw new IOException("Failed to load node object from file");
            }

            /* Get routingTable object from storageHandler */
            RoutingTable newRoutingTable = storageHandler.getRoutingTable();

            if (newRoutingTable != null) {
                logger.info("Routing table read successfully");
                routingTable = newRoutingTable;
            } else {
                throw new IOException("Failed to load routing table object from file");
            }

            /* Get users object from storageHandler */
            Users newUsers = storageHandler.getUsers();

            if (newUsers != null) {
                logger.info("Users read successfully");
                users = newUsers;
            } else {
                throw new IOException("Failed to read users object from file");
            }

            /* Get users object from storageHandler */
            Messages newMessages = storageHandler.getMessages();

            if (newMessages != null) {
                logger.info("Messages read successfully");
                messages = newMessages;
                messages.init(this);
            } else {
                throw new IOException("Failed to read messages object from file");
            }
    }

    /**
     * Saves the appropriate objects to a file using the StorageHandler class
     */
    private void saveState() throws IOException {
        logger.info("Saving state to file");
        storageHandler.save(config, networkId, node, routingTable, users, messages);
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
        /* As this is a new network we need to generate a networkId */
        networkId = new KeyId();

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

        logger.info("Bootstrapping localnode {} to node {}", node.toString(), node.toString());
        ConnectOperation connect = new ConnectOperation(server, this, node, config);
        connect.execute();

        return connect.isError();
    }

    /**
     * Shuts down the server cleanly
     */
    public void shutdown(boolean saveState) throws IOException {
        server.shutdown();  // Shut down the listener
        stopRefreshOperation(); // Stop the automatic refresh timer

        if (saveState) {
            saveState(); // Save the node and routingTable objects to file
        }

        logger.info("Server shut down successfully");
    }

    public KeyId getNetworkId() {
        return networkId;
    }

    public void setNetworkId(KeyId networkId) {
        this.networkId = networkId;
    }

    public Configuration getConfig() {
        return config;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
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

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public Encryption getEncryption() {
        return encryption;
    }
}
