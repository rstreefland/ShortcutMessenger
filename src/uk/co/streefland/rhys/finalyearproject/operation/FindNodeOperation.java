package uk.co.streefland.rhys.finalyearproject.operation;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;
import uk.co.streefland.rhys.finalyearproject.message.FindNodeMessage;
import uk.co.streefland.rhys.finalyearproject.message.FindNodeReplyMessage;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.node.KeyComparator;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;

import java.io.IOException;
import java.util.*;

/**
 * Finds the K closest nodes to a specified NodeId
 * Terminates when it has responses from the K closest nodes it has seen.
 * Nodes that fail to respond are removed from consideration
 */
public class FindNodeOperation implements Operation, Receiver {

    // Flags that represent Node state
    private static final String NOT_QUERIED = "1";
    private static final String AWAITING_RESPONSE = "2";
    private static final String QUERIED = "3";
    private static final String FAILED = "4";

    private final Server server;
    private final LocalNode localNode;
    private final Configuration config;

    private final Message lookupMessage;        // Message sent to each peer
    private final Map<Node, String> nodes;

    /* Tracks messages in transit and awaiting reply */
    private final Map<Integer, Node> messagesInTransit;

    /* Used to sort nodes */
    private final Comparator comparator;

    public FindNodeOperation(Server server, LocalNode localNode, NodeId lookupId, Configuration config) {
        this.server = server;
        this.localNode = localNode;
        this.config = config;

        this.lookupMessage = new FindNodeMessage(localNode.getNode(), lookupId);
        this.messagesInTransit = new HashMap<>();

        /* Initialise a TreeMap that is sorted by which nodes are closest to the lookupId */
        this.comparator = new KeyComparator(lookupId);
        this.nodes = new TreeMap(this.comparator);
    }

    /**
     * Runs the find node operation
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {
        try {
            /* Set the local node as already asked */
            nodes.put(this.localNode.getNode(), QUERIED);

            /* Insert all nodes because some nodes may fail to respond. */
            this.addNodes(this.localNode.getRoutingTable().getAllNodes());

            /* If we haven't finished as yet, wait for a maximum of config.operationTimeout() time */
            int totalTimeWaited = 0;
            int timeInterval = 10;     // We re-check every n milliseconds
            while (totalTimeWaited < this.config.getOperationTimeout()) {
                if (!this.iterativeQueryNodes()) {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                } else {
                    break;
                }
            }

            /* Now after we've finished, we would have an idea of offline nodes, lets update our routing table */
            this.localNode.getRoutingTable().setUnresponsiveContacts(this.getFailedNodes());

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Inserts the nodes into the TreeMap if they're not already present
     * @param list The list of nodes to insert
     */
    private void addNodes(List<Node> list) {
        for (Node node : list) {
            if (!nodes.containsKey(node)) {
                nodes.put(node, NOT_QUERIED);
            }
        }
    }

    /**
     * Sends a message to every not queried node. Maintains a maximum of config.getMaxConcurrency() active messages in transit
     * @return false if algorithm isn't finished, true if algorithm has finished
     * @throws IOException
     */
    private boolean iterativeQueryNodes() throws IOException {
        /* Maximum number of messages already in transit */
        if (this.config.getMaxConcurrency() <= this.messagesInTransit.size()) {
            return false;
        }

        List<Node> notQueried = this.getClosestNodes(NOT_QUERIED);

        /* No not queried nodes nor any messages in transit - finish */
        if (notQueried.isEmpty() && this.messagesInTransit.isEmpty()) {
            return true;
        }

        /* Create new messages for every not queried node, not exceeding config.getMaxConcurrency() */
        for (int i = 0; (this.messagesInTransit.size() < this.config.getMaxConcurrency()) && (i < notQueried.size()); i++) {
            Node n = notQueried.get(i);

            int communicationId = server.sendMessage(n, lookupMessage, this);

            this.nodes.put(n, AWAITING_RESPONSE);
            this.messagesInTransit.put(communicationId, n);
        }

        return false;
    }

    /**
     * @param status The status of the nodes to return
     * @return The K closest nodes to the target lookupId given that have the specified status
     */
    private List<Node> getClosestNodes(String status) {
        List<Node> closestNodes = new ArrayList<>(this.config.getK());
        int remainingSpaces = this.config.getK();

        for (Map.Entry e : this.nodes.entrySet()) {
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
     * @return A list of failed nodes
     */
    private List<Node> getFailedNodes() {
        List<Node> failedNodes = new ArrayList<>();

        for (Map.Entry<Node, String> e : this.nodes.entrySet()) {
            if (e.getValue().equals(FAILED)) {
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
            System.out.println("INCOMING MESSAGE WAS A DIFFERENT TYPE");
            /* Not sure why we get a message of a different type here... todo Figure it out. */
            return;
        }

        /* Read the FindNodeReplyMessage */
        FindNodeReplyMessage msg = (FindNodeReplyMessage) incoming;

        /* Add the origin node to our routing table */
        Node origin = msg.getOrigin();
        this.localNode.getRoutingTable().insert(origin);

        /* Set that we've completed ASKing the origin node */
        this.nodes.put(origin, QUERIED);

        /* Remove this msg from messagesTransiting since it's completed now */
        this.messagesInTransit.remove(communicationId);

        /* Add the received nodes to our nodes list to query */
        this.addNodes(msg.getNodes());
    }

    /**
     * A node did not respond or a packet was lost, set this node as failed
     *
     * @throws IOException
     */
    @Override
    public synchronized void timeout(int communicationId) throws IOException {
        /* Get the node associated with this communication */
        Node n = this.messagesInTransit.get(communicationId);

        if (n == null) {
            return;
        }

        /* Mark this node as failed and inform the routing table that it is unresponsive */
        this.nodes.put(n, FAILED);
        this.localNode.getRoutingTable().setUnresponsiveContact(n);
        this.messagesInTransit.remove(communicationId);
    }
}
