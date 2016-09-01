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
 * Finds the K closest nodes to a specified identifier
 * The algorithm terminates when it has gotten responses from the K closest nodes it has seen.
 * Nodes that fail to respond are removed from consideration
 */
public class FindNodeOperation implements Operation, Receiver {

    //flags
    private static final String NOTQUERIED = "1";
    private static final String AWAITINGRESPONSE = "2";
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

    /**
     * @param server    KadServer used for communication
     * @param localNode The local node making the communication
     * @param lookupId  The ID for which to find nodes close to
     * @param config
     */
    public FindNodeOperation(Server server, LocalNode localNode, NodeId lookupId, Configuration config) {
        this.server = server;
        this.localNode = localNode;
        this.config = config;

        this.lookupMessage = new FindNodeMessage(localNode.getNode(), lookupId);
        this.messagesInTransit = new HashMap<>();

        /**
         * We initialize a TreeMap to store nodes.
         * This map will be sorted by which nodes are closest to the lookupId
         */
        this.comparator = new KeyComparator(lookupId);
        this.nodes = new TreeMap(this.comparator);
    }

    /**
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {
        try {
            /* Set the local node as already asked */
            nodes.put(this.localNode.getNode(), QUERIED);

            /**
             * We add all nodes here instead of the K-Closest because there may be the case that the K-Closest are offline
             * - The operation takes care of looking at the K-Closest.
             */
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

    private void addNodes(List<Node> list) {
        for (Node node : list) {
            /* If this node is not in the list, add the node */
            if (!nodes.containsKey(node)) {
                nodes.put(node, NOTQUERIED);
            }
        }
    }

    private boolean iterativeQueryNodes() throws IOException {
        if (this.config.getMaxConcurrency() <= this.messagesInTransit.size()) {
            return false;
        }

        List<Node> notQueried = this.getClosestNodes(NOTQUERIED);

        if (notQueried.isEmpty() && this.messagesInTransit.isEmpty()) {
            /* We have no unasked nodes nor any messages in transit, we're finished! */
            return true;
        }

        for (int i = 0; (this.messagesInTransit.size() < this.config.getMaxConcurrency()) && (i < notQueried.size()); i++) {
            Node n = notQueried.get(i);

            int communicationId = server.sendMessage(n, lookupMessage, this);

            this.nodes.put(n, AWAITINGRESPONSE);
            this.messagesInTransit.put(communicationId, n);
        }

        /* We're not finished as yet, return false */
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
                /* We got one with the required status, now add it */
                closestNodes.add((Node) e.getKey());
                if (--remainingSpaces == 0) {
                    break;
                }
            }
        }

        return closestNodes;
    }

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
        if (!(incoming instanceof FindNodeReplyMessage))
        {
            /* Not sure why we get a message of a different type here... @todo Figure it out. */
            return;
        }

        /* We receive a FindNodeReplyMessage with a set of nodes, read this message */
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
        //this.iterativeQueryNodes();
    }

    /**
     * A node does not respond or a packet was lost, we set this node as failed
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

       // this.iterativeQueryNodes();
    }
}
