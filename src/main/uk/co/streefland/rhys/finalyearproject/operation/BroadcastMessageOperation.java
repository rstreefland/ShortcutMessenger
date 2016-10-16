package uk.co.streefland.rhys.finalyearproject.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.message.AcknowledgeMessage;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.message.TextMessage;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends a simple broadcast message to all nodes in the local node' routing table
 * This class only really exists for testing and will be removed later on in the development process.
 */
public class BroadcastMessageOperation implements Operation, Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Server server;
    private final Configuration config;
    private final Message message;
    private final Map<Node, String> nodes;
    private final Map<Node, Integer> attempts;

    /* Tracks messages in transit and awaiting reply */
    private final Map<Integer, Node> messagesInTransit;

    public BroadcastMessageOperation(LocalNode localNode, String message, List<Node> targetNodes) {
        this.server = localNode.getServer();
        this.config = localNode.getConfig();
        this.message = new TextMessage(localNode.getNode(), message);
        this.nodes = new HashMap<>();
        this.attempts = new HashMap<>();
        this.messagesInTransit = new HashMap<>();

        /* Set the local node as already messaged because we don't want to message the local node */
        nodes.put(localNode.getNode(), Configuration.QUERIED);
        attempts.put(localNode.getNode(), 0);

        /* Add the target nodes to the HashMaps */
        addNodes(targetNodes);
    }

    /**
     * Send the node message to the target node and wait for a reply. Run FindNodeOperation and BucketRefreshOperation if the target node responds to populate the local RoutingTable
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {
        try {
            /* If operation hasn't finished, wait for a maximum of config.operationTimeout() time */
            int totalTimeWaited = 0;
            int timeInterval = 10;
            while (totalTimeWaited < config.getOperationTimeout()) {
                if (!iterativeMessagesNodes()) {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                } else {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("BroadcastMessageOperation was interrupted unexpectedly: {}", e);
        }

    }

    /**
     * Inserts the nodes into the HashMap if they're not already present
     *
     * @param list The list of nodes to insert
     */
    private void addNodes(List<Node> list) {
        for (Node node : list) {
            if (!nodes.containsKey(node)) {
                nodes.put(node, Configuration.NOT_QUERIED);
            }

            if (!attempts.containsKey(node)) {
                attempts.put(node, 0);
            }
        }
    }

    private boolean iterativeMessagesNodes() throws IOException {
        /* Maximum number of messages already in transit */
        if (Configuration.MAX_CONCURRENCY <= messagesInTransit.size()) {
            return false;
        }

        List<Node> toMessage = new ArrayList<>();

        for (Map.Entry<Node, String> e : nodes.entrySet()) {
            if (e.getValue().equals(Configuration.NOT_QUERIED) || e.getValue().equals(Configuration.FAILED)) {
                if (attempts.get(e.getKey()) < config.getMaxConnectionAttempts()) {
                    toMessage.add(e.getKey());
                }
            }
        }

        /* No not messaged nodes nor any messages in transit - finish */
        if (toMessage.isEmpty() && messagesInTransit.isEmpty()) {
            return true;
        }

        /* Create new messages for every not queried node, not exceeding Configuration.MAX_CONCURRENCY */
        for (int i = 0; (messagesInTransit.size() < Configuration.MAX_CONCURRENCY) && (i < toMessage.size()); i++) {

            int communicationId = server.sendMessage(toMessage.get(i), message, this);

            nodes.put(toMessage.get(i), Configuration.AWAITING_REPLY);
            attempts.put(toMessage.get(i), attempts.get(toMessage.get(i)) + 1);
            messagesInTransit.put(communicationId, toMessage.get(i));
        }
        return true;
    }

    /**
     * Receives and handles the reply from the target node
     *
     * @param communicationId
     */
    @Override
    public synchronized void receive(Message incoming, int communicationId) {
        /* Read the AcknowledgeMessage */
        AcknowledgeMessage msg = (AcknowledgeMessage) incoming;

        logger.debug("ACK received from {}", msg.getOrigin().getSocketAddress().getHostName());

        /* Update the hashmap to show that we've finished messaging this node */
        nodes.put(msg.getOrigin(), Configuration.QUERIED);

         /* Remove this msg from messagesTransiting since it's completed now */
        messagesInTransit.remove(communicationId);

        notify();
    }

    /**
     * Runs if the two second timeout occurs. Sends the message again if attempts < maxConnectionAttempts (5)
     *
     * @param communicationId
     * @throws IOException
     */
    @Override
    public synchronized void timeout(int communicationId) {
        /* Get the node associated with this communication */
        Node n = messagesInTransit.get(communicationId);

        if (n == null) {
            return;
        }

        /* Mark this node as failed, increment attempts, remove message in transit */
        nodes.put(n, Configuration.FAILED);
        attempts.put(n, attempts.get(n) + 1);
        messagesInTransit.remove(communicationId);
    }
}

