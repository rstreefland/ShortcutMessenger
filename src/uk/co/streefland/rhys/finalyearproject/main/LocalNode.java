package uk.co.streefland.rhys.finalyearproject.main;

import com.sun.deploy.config.DefaultConfig;
import uk.co.streefland.rhys.finalyearproject.message.MessageFactory;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;
import uk.co.streefland.rhys.finalyearproject.operation.ConnectOperation;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;
import uk.co.streefland.rhys.finalyearproject.routing.RoutingTable;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Rhys on 30/08/2016.
 */
public class LocalNode {

    /* Kademlia Attributes */
    private final String ownerId;

    /* Objects to be used */
    private final transient Node localNode;
    private final transient Server server;
    //private final transient KademliaDHT dht;
    private transient RoutingTable routingTable;
    private final int udpPort;
    private transient Configuration config;

    /* Timer used to execute refresh operations */
    private transient Timer refreshOperationTimer;
    private transient TimerTask refreshOperationTTask;

    /* Factories */
    private final transient MessageFactory messageFactory;

    public LocalNode(String ownerId, Node localNode, int udpPort, RoutingTable routingTable, Configuration config) throws IOException
    {
        this.ownerId = ownerId;
        this.udpPort = udpPort;
        this.localNode = localNode;
        //this.dht = dht;
        this.config = config;
        this.routingTable = routingTable;
        this.messageFactory = new MessageFactory(this, this.config);
        this.server = new Server(udpPort, this.messageFactory, this.localNode, this.config);
        //this.startRefreshOperation();
    }

    public LocalNode(String ownerId, NodeId defaultId, int udpPort) throws IOException {
        this.ownerId = ownerId;
        this.localNode = new Node(defaultId, InetAddress.getLocalHost(), udpPort);
        this.udpPort = udpPort;
        this.config = new Configuration();
        this.routingTable = new RoutingTable(this.localNode, this.config);

        this.messageFactory = new MessageFactory(this, this.config);
        this.server = new Server(udpPort, this.messageFactory, this.localNode, this.config);
    }

    public synchronized final void bootstrap(Node n) throws IOException
    {
        Operation op = new ConnectOperation(this.server, this, n, this.config);
        op.execute();
    }

    public Configuration getConfig() {
        return config;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
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
