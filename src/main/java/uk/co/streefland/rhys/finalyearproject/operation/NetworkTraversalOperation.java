package uk.co.streefland.rhys.finalyearproject.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.message.node.FindNodeMessage;
import uk.co.streefland.rhys.finalyearproject.message.node.FindNodeMessageReply;
import uk.co.streefland.rhys.finalyearproject.message.node.NetworkTraversalMessage;
import uk.co.streefland.rhys.finalyearproject.message.node.NetworkTraversalMessageReply;
import uk.co.streefland.rhys.finalyearproject.node.KeyComparator;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.util.*;

/**
 * Queries every node in the RoutingTable for their RoutingTablee iteratively. Designed to create a list of the entire network for
 */
public class NetworkTraversalOperation implements Operation, Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LocalNode localNode;
    private final Server server;
    private final Message lookupMessage;
    private final Map<Node, Configuration.Status> nodes;
    private final Map<KeyId, List<Node>> nodeRoutingTables;
    private final Map<Integer, Node> messagesInTransit;

    public NetworkTraversalOperation(LocalNode localNode) {
        this.localNode = localNode;
        this.server = localNode.getServer();

        this.lookupMessage = new NetworkTraversalMessage(localNode.getNetworkId(), localNode.getNode());
        this.messagesInTransit = new HashMap<>();
        this.nodeRoutingTables = new HashMap<>();

        /* Initialise a TreeMap that is sorted by which nodes are closest to the local nodeId */
        Comparator<Node> comparator = new KeyComparator(localNode.getNode().getNodeId());
        this.nodes = new TreeMap<>(comparator);
    }

    /**
     * Runs the find node operation
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {
        long start = System.currentTimeMillis();

        /* Set the local node as already asked */
        nodes.put(localNode.getNode(), Configuration.Status.QUERIED);

        /* Add the local routing table to the list */
        nodeRoutingTables.put(localNode.getNode().getNodeId(), localNode.getRoutingTable().getAllNodes(false));

        /* Insert all nodes */
        addNodes(localNode.getRoutingTable().getAllNodes(false));

        try {
            /* Runs until it's queried (or failed to query) ever node - this could be problematic */
            int timeInterval = 10;
            while (true) {
                if (!iterativeQueryNodes()) {
                    wait(timeInterval);
                } else {
                    break;
                }
            }

        } catch (InterruptedException e) {
            logger.error("NetworkTraversalOperation was interrupted: {} " + e.getMessage());
        }

        long end = System.currentTimeMillis();
        long time = end-start;

        logger.info("NetworkTraversalOperation took: " + time + "ms");
    }

    /**
     * Inserts the nodes into the TreeMap if they're not already present
     *
     * @param list The list of nodes to insert
     */
    private void addNodes(List<Node> list) {
        for (Node node : list) {
            if (!nodes.containsKey(node)) {
                nodes.put(node, Configuration.Status.NOT_QUERIED);
            }
        }
    }

    /**
     * Sends a message to every not queried node. Maintains a maximum of Configuration.MAX_CONCURRENCY active messages in transit
     *
     * @return false if algorithm isn't finished, true if algorithm has finished
     * @throws IOException
     */
    private boolean iterativeQueryNodes() throws IOException {
        /* Maximum number of messages already in transit */
        if (Configuration.MAX_CONCURRENCY <= messagesInTransit.size()) {
            return false;
        }

        List<Node> notQueried = getClosestNodes(Configuration.Status.NOT_QUERIED);

        /* No not queried nodes nor any messages in transit - finish */
        if (notQueried.isEmpty() && messagesInTransit.isEmpty()) {
            return true;
        }

        /* Create new messages for every not queried node, not exceeding Configuration.MAX_CONCURRENCY */
        for (int i = 0; (messagesInTransit.size() < Configuration.MAX_CONCURRENCY) && (i < notQueried.size()); i++) {

            Node n = notQueried.get(i);

            /* Don't message a node with the same IP address as the local node because it's stale  - mark it as unresponsive */
            if (n.getPublicInetAddress().equals(localNode.getNode().getPublicInetAddress()) && n.getPrivateInetAddress().equals(localNode.getNode().getPrivateInetAddress()) && n.getPrivatePort() == localNode.getNode().getPrivatePort()) {
                //logger.info("Not running find node operation against stale node");
                localNode.getRoutingTable().setUnresponsiveContact(n);
                nodes.put(n, Configuration.Status.QUERIED);
            } else {
                int communicationId = server.sendMessage(n, lookupMessage, this);

                nodes.put(n, Configuration.Status.AWAITING_REPLY);
                messagesInTransit.put(communicationId, n);
            }
        }
        return false;
    }

    /**
     * @param status The status of the nodes to return
     * @return The K closest nodes to the target lookupId given that have the specified status
     */
    private List<Node> getClosestNodes(Configuration.Status status) {
        List<Node> closestNodes = new ArrayList<>(Configuration.K);
        int remainingSpaces = Configuration.K;

        for (Map.Entry e : nodes.entrySet()) {
            if (status.equals(e.getValue())) {
                /* Found node with the required status, now add it */
                closestNodes.add((Node) e.getKey());
                if (--remainingSpaces == 0) {
                    break;
                }
            }
        }

        return closestNodes;
    }

    /**
     * Receive and handle the incoming FindNodeMessageReply
     *
     * @throws IOException
     */
    @Override
    public synchronized void receive(Message incoming, int communicationId) throws IOException {
        NetworkTraversalMessageReply msg = (NetworkTraversalMessageReply) incoming;

        Node origin = msg.getOrigin();

        /* Set that we've completed asking the origin node */
        nodes.put(origin, Configuration.Status.QUERIED);

        /* Remove this msg from messagesTransiting since it's completed now */
        messagesInTransit.remove(communicationId);

        /* Add the received nodes to our nodes list to query */
        addNodes(msg.getNodes());

        /* Add the received routing table to nodeRoutingTables */
        nodeRoutingTables.put(msg.getOrigin().getNodeId(), msg.getNodes());

        /* Wake up waiting thread */
        notify();
    }

    /**
     * A node did not respond or a packet was lost, set this node as failed
     *
     * @throws IOException
     */
    @Override
    public synchronized void timeout(int communicationId) throws IOException {
        /* Get the node associated with this communication */
        Node n = messagesInTransit.get(communicationId);

        if (n == null) {
            return;
        }

        /* Mark this node as failed */
        nodes.put(n, Configuration.Status.FAILED);
        messagesInTransit.remove(communicationId);

        /* Wake up waiting thread */
        notify();
    }

    public Map<KeyId, List<Node>> getNodeRoutingTables() {
        return nodeRoutingTables;
    }
}