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
 * Created by Rhys on 03/09/2016.
 */
public class TextMessageOperation implements Operation, Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /* Flags that represent Node states */
    private static final String NOT_MESSAGED = "1";
    private static final String AWAITING_ACK = "2";
    private static final String MESSAGED = "3";
    private static final String FAILED = "4";

    private Server server;
    private Configuration config;

    private Message message;        // Message sent to each peer
    private Map<Node, String> nodes;
    private Map<Node, Integer> attempts;

    /* Tracks messages in transit and awaiting reply */
    private Map<Integer, Node> messagesInTransit;

    public TextMessageOperation(Server server, LocalNode localNode, Configuration config, String message, List<Node> targetNodes) {
        this.server = server;
        this.config = config;

        this.message = new TextMessage(localNode.getNode(), message);
        this.nodes = new HashMap<>();
        this.attempts = new HashMap<>();

        this.messagesInTransit = new HashMap<>();

        /* Set the local node as already messaged because we don't want to message the local node */
        nodes.put(localNode.getNode(), MESSAGED);
        attempts.put(localNode.getNode(), 0);

        /* Add the target nodes to the HashMaps */
        addNodes(targetNodes);
    }

    /**
     * Send the connect message to the target node and wait for a reply. Run FindNodeOperation and BucketRefreshOperation if the target node responds to populate the local RoutingTable
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {
        try {
            /* If we haven't finished as yet, wait for a maximum of config.operationTimeout() time */
            int totalTimeWaited = 0;
            int timeInterval = 10;     // We re-check every n milliseconds
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
            logger.error("TextMessageOperation was interrupted unexpectedly: {}", e);
        }

    }

    /**
     * Inserts the nodes into the HashMap if they're not already present
     * @param list The list of nodes to insert
     */
    private void addNodes(List<Node> list) {
        for (Node node : list) {
            if (!nodes.containsKey(node)) {
                nodes.put(node, NOT_MESSAGED);
            }

            if (!attempts.containsKey(node)) {
                attempts.put(node, 0);
            }
        }
    }

    private boolean iterativeMessagesNodes() throws IOException {
        /* Maximum number of messages already in transit */
        if (config.getMaxConcurrency() <= messagesInTransit.size()) {
            return false;
        }

        List<Node> toMessage = new ArrayList<>();

        for (Map.Entry<Node, String> e: nodes.entrySet()) {
            if (e.getValue().equals(NOT_MESSAGED) || e.getValue().equals(FAILED)) {
                if (attempts.get(e.getKey()) < config.getMaxConnectionAttempts()) {
                    toMessage.add(e.getKey());
                }
            }
        }

        /* No not messaged nodes nor any messages in transit - finish */
        if (toMessage.isEmpty() && messagesInTransit.isEmpty()) {
            return true;
        }

        /* Create new messages for every not queried node, not exceeding config.getMaxConcurrency() */
        for (int i = 0; (messagesInTransit.size() < config.getMaxConcurrency()) && (i < toMessage.size()); i++) {

            int communicationId = server.sendMessage(toMessage.get(i), message, this);

            nodes.put(toMessage.get(i), AWAITING_ACK);
            attempts.put(toMessage.get(i), attempts.get(toMessage.get(i)) + 1);
            messagesInTransit.put(communicationId, toMessage.get(i));
        }
        return true;
    }

    /**
     * Receives an AcknowledgeMessage from the target node
     *
     * @param communicationId
     */
    @Override
    public synchronized void receive(Message incoming, int communicationId) {
        /* Read the AcknowledgeMessage */
        AcknowledgeMessage msg = (AcknowledgeMessage) incoming;

        logger.info("ACK received from {}", msg.getOrigin().getSocketAddress().getHostName());

        /* Update the hashmap to show that we've finished messaging this node */
        nodes.put(msg.getOrigin(), MESSAGED);

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

        /* Mark this node as failed */
        nodes.put(n, FAILED);
        attempts.put(n, attempts.get(n) + 1);
        messagesInTransit.remove(communicationId);
    }
}

