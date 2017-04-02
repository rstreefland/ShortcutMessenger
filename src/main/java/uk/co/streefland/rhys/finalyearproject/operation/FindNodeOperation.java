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
    private final Message lookupMessage;
    private final Map<Node, Configuration.Status> nodes;
    private final Map<Integer, Node> messagesInTransit;
    private final boolean ignoreStale;

    public FindNodeOperation(LocalNode localNode, KeyId lookupId, boolean ignoreStale) {
        this.localNode = localNode;
        this.server = localNode.getServer();
        this.config = localNode.getConfig();

        this.lookupMessage = new FindNodeMessage(localNode.getNetworkId(), localNode.getNode(), lookupId);
        this.messagesInTransit = new HashMap<>();

        this.ignoreStale = ignoreStale;

        /* Initialise a TreeMap that is sorted by which nodes are closest to the lookupId */
        Comparator<Node> comparator = new KeyComparator(lookupId);
        this.nodes = new TreeMap<>(comparator);
    }

    /**
     * Runs the find node operation
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {
        /* Set the local node as already asked */
        nodes.put(localNode.getNode(), Configuration.Status.FAILED);
        localNode.getRoutingTable().setUnresponsiveContact(localNode.getNode());

        /* Insert all nodes because some nodes may fail to respond. */
        addNodes(localNode.getRoutingTable().getAllNodes(ignoreStale));

        try {
            /* If operation hasn't finished, wait for a maximum of config.getOperationTimeout() time */
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
                nodes.putIfAbsent(node, Configuration.Status.NOT_QUERIED);
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
            if (n.getPublicInetAddress().equals(localNode.getNode().getPublicInetAddress()) && n.getPrivateInetAddress().equals(localNode.getNode().getPrivateInetAddress())) {
                //logger.info("Not running find node operation against stale node");
                localNode.getRoutingTable().setUnresponsiveContact(n);
                nodes.put(n, Configuration.Status.FAILED);
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

    public List<Node> getClosestNodes() {
        return getClosestNodes(Configuration.Status.QUERIED);
    }

    /**
     * @return A list of failed nodes
     */
    private List<Node> getFailedNodes() {
        List<Node> failedNodes = new ArrayList<>();

        for (Map.Entry<Node, Configuration.Status> e : nodes.entrySet()) {
            if (e.getValue().equals(Configuration.Status.FAILED)) {
                failedNodes.add(e.getKey());
            }
        }

        return failedNodes;
    }

    /**
     * Receive and handle the incoming FindNodeMessageReply
     *
     * @throws IOException
     */
    @Override
    public synchronized void receive(Message incoming, int communicationId) throws IOException {

        /* Read the FindNodeMessageReply */
        FindNodeMessageReply msg = (FindNodeMessageReply) incoming;

        /* Add the origin node to our routing table */
        Node origin = msg.getOrigin();
        localNode.getRoutingTable().insert(origin);

        /* Set that we've completed ASKing the origin node */
        nodes.put(origin, Configuration.Status.QUERIED);

        /* Remove message from messagesInTransit */
        messagesInTransit.remove(communicationId);

        /* Add the received nodes to our nodes list to query */
        addNodes(msg.getNodes());

        /* Run the lookup against the new nodes */
        iterativeQueryNodes();

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

        /* Mark this node as failed and inform the routing table that it is unresponsive */
        nodes.put(n, Configuration.Status.FAILED);
        localNode.getRoutingTable().setUnresponsiveContact(n);
        messagesInTransit.remove(communicationId);

        /* Run the lookup again */
        iterativeQueryNodes();

        /* Wake up waiting thread */
        notify();
    }
}
