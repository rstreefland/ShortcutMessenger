package uk.co.streefland.rhys.finalyearproject.main;

import uk.co.streefland.rhys.finalyearproject.message.MessageHandler;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;
import uk.co.streefland.rhys.finalyearproject.operation.ConnectOperation;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;
import uk.co.streefland.rhys.finalyearproject.operation.RefreshHandler;
import uk.co.streefland.rhys.finalyearproject.routing.RoutingTable;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;

/**
 * Represents the local node on the network. This class ties together all of the functionality of the other classes
 */
public class LocalNode {

    private final String ownerId;
    private final int port;

    private Configuration config;
    private final Node localNode;
    private RoutingTable routingTable;
    private final Server server;

    /* Objects for refresh operation */
    private Timer refreshOperationTimer;
    private RefreshHandler refreshHandler;

    /* MessageHandler object to create and receive messages */
    private final MessageHandler messageHandler;

    public LocalNode(String ownerId, Node localNode, int port, RoutingTable routingTable, Configuration config) throws IOException {
        this.ownerId = ownerId;
        this.port = port;

        this.config = config;
        this.localNode = localNode;
        this.routingTable = routingTable;
        this.messageHandler = new MessageHandler(this, config);
        this.server = new Server(port, messageHandler, localNode, config);

        startRefreshOperation();
    }

    public LocalNode(String ownerId, NodeId defaultId, int port) throws IOException {
        this.ownerId = ownerId;
        this.localNode = new Node(defaultId, InetAddress.getLocalHost(), port);
        this.port = port;
        this.config = new Configuration();
        this.routingTable = new RoutingTable(localNode, config);

        this.messageHandler = new MessageHandler(this, config);
        this.server = new Server(port, messageHandler, localNode, config);

        startRefreshOperation();
    }

    /**
     * Starts the automatic refresh operation
     */
    private void startRefreshOperation() {
        /* Create the refresh operation */
        this.refreshHandler = new RefreshHandler(this.server, this, this.config);

        /* Create the timer and schedule it using the interval defined in Configuration.java  */
        this.refreshOperationTimer = new Timer(true);
        this.refreshOperationTimer.schedule(this.refreshHandler, this.config.getRefreshInterval(), this.config.getRefreshInterval());
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
        Operation connect = new ConnectOperation(this.server, this, node, this.config);
        connect.execute();
    }

    /**
     * Shuts down the server cleanly
     */
    public void shutdown()
    {
        /* Shut down the server */
        server.shutdown();
        stopRefreshOperation();

        // TODO: 31/08/2016 You'll want to save the state of the system to file here
    }

    public Configuration getConfig() {
        return config;
    }

    public int getPort() {
        return port;
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

    public String getOwnerId() {
        return ownerId;
    }
}
