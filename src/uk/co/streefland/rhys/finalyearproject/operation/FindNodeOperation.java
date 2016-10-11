package uk.co.streefland.rhys.finalyearproject.operation;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.message.node.FindNodeMessage;
import uk.co.streefland.rhys.finalyearproject.message.node.FindNodeReplyMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyComparator;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.util.*;

/**
 * Finds the K closest nodes to a specified KeyId
 * Terminates when it has responses from the K closest nodes it has seen.
 * Nodes that fail to respond are removed from consideration
 */
public class FindNodeOperation implements Operation, Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LocalNode localNode;
    private final Server server;
    private final Configuration config;
    private final Message lookupMessage;        // Message sent to each peer
    private final Map<Node, String> nodes;

    /* Tracks messages in transit and awaiting reply */
    private final Map<Integer, Node> messagesInTransit;

    /* Used to sort nodes */
    private final Comparator comparator;

    public FindNodeOperation(LocalNode localNode, KeyId lookupId) {
        this.localNode = localNode;
        this.server = localNode.getServer();
        this.config = localNode.getConfig();

        this.lookupMessage = new FindNodeMessage(localNode.getNode(), lookupId);
        this.messagesInTransit = new HashMap<>();

        /* Initialise a TreeMap that is sorted by which nodes are closest to the lookupId */
        this.comparator = new KeyComparator(lookupId);
        this.nodes = new TreeMap(this.comparator);
    }

    /**
     * Runs the find node operation
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {
        /* Set the local node as already asked */
        nodes.put(localNode.getNode(), Configuration.QUERIED);

        /* Insert all nodes because some nodes may fail to respond. */
        addNodes(localNode.getRoutingTable().getAllNodes());

        try {
            /* If operation hasn't finished, wait for a maximum of config.operationTimeout() time */
            int totalTimeWaited = 0;
            int timeInterval = 10;
            while (totalTimeWaited < config.getOperationTimeout()) {
                if (!iterativeQueryNodes()) {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                } else {
                    break;
                }
            }

            /* Now after we've finished, we would have an idea of offline nodes, lets update our routing table */
            localNode.getRoutingTable().setUnresponsiveContacts(getFailedNodes());

        } catch (InterruptedException e) {
            logger.error("Find node operation was interrupted: {} " + e.getMessage());
        }
    }

    /**
     * Inserts the nodes into the TreeMap if they're not already present
     *
     * @param list The list of nodes to insert
     */
    private void addNodes(List<Node> list) {
        for (Node node : list) {
            if (!nodes.containsKey(node)) {
                nodes.put(node, Configuration.NOT_QUERIED);
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

        List<Node> notQueried = getClosestNodes(Configuration.NOT_QUERIED);

        /* No not queried nodes nor any messages in transit - finish */
        if (notQueried.isEmpty() && messagesInTransit.isEmpty()) {
            return true;
        }

        /* Create new messages for every not queried node, not exceeding Configuration.MAX_CONCURRENCY */
        for (int i = 0; (messagesInTransit.size() < Configuration.MAX_CONCURRENCY) && (i < notQueried.size()); i++) {
            Node n = notQueried.get(i);

            int communicationId = server.sendMessage(n, lookupMessage, this);

            nodes.put(n, Configuration.AWAITING_REPLY);
            messagesInTransit.put(communicationId, n);
        }
        return false;
    }

    /**
     * @param status The status of the nodes to return
     * @return The K closest nodes to the target lookupId given that have the specified status
     */
    private List<Node> getClosestNodes(String status) {
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

    public List<Node> getClosestNodes() {
        return getClosestNodes(Configuration.QUERIED);
    }

    /**
     * @return A list of failed nodes
     */
    private List<Node> getFailedNodes() {
        List<Node> failedNodes = new ArrayList<>();

        for (Map.Entry<Node, String> e : nodes.entrySet()) {
            if (e.getValue().equals(Configuration.FAILED)) {
                failedNodes.add(e.getKey());
            }
        }

        return failedNodes;
    }

    /**
     * Receive and handle the incoming FindNodeReplyMessage
     *
     * @throws IOException
     */
    @Override
    public synchronized void receive(Message incoming, int communicationId) throws IOException {
        if (!(incoming instanceof FindNodeReplyMessage)) {

            logger.warn("Incoming message was of a different type");
            logger.warn("{}", incoming.getClass().toString());

            /* I know why this is happening - fix in progress*/
            return;
        }

        /* Read the FindNodeReplyMessage */
        FindNodeReplyMessage msg = (FindNodeReplyMessage) incoming;

        /* Add the origin node to our routing table */
        Node origin = msg.getOrigin();
        localNode.getRoutingTable().insert(origin);

        /* Set that we've completed ASKing the origin nodse */
        nodes.put(origin, Configuration.QUERIED);

        /* Remove this msg from messagesTransiting since it's completed now */
        messagesInTransit.remove(communicationId);

        /* Add the received nodes to our nodes list to query */
        addNodes(msg.getNodes());

        /* Run the lookup against the new nodes */
        iterativeQueryNodes();
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

        /* Mark this node as failed and inform the routing table that it is unresponsive */
        nodes.put(n, Configuration.FAILED);
        localNode.getRoutingTable().setUnresponsiveContact(n);
        messagesInTransit.remove(communicationId);

        /* Run the lookup again */
        iterativeQueryNodes();
    }
}
