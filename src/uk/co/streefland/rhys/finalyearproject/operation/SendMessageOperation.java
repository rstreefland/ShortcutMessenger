package uk.co.streefland.rhys.finalyearproject.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.core.User;
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
public class SendMessageOperation implements Operation, Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /* Flags that represent Node states */
    private static final String NOT_QUERIED = "1";
    private static final String AWAITING_ACK = "2";
    private static final String QUERIED = "3";
    private static final String FAILED = "4";

    private Server server;
    private Configuration config;
    private LocalNode localNode;
    private User userToMessage;
    private User user;

    private String textMessage;
    private TextMessage message; // Message sent to each peer
    private Map<Node, String> nodes;
    private Map<Node, Integer> attempts;

    private Map<Integer, Node> messagesInTransit;
    private boolean forwarding;
    private boolean isMessagedSuccessfully;
    private boolean hasTimeoutOccurred;

    private List closestNodes;

    public SendMessageOperation(Server server, LocalNode localNode, Configuration config, User userToMessage, String textMessage, boolean forwarding) {
        this.server = server;
        this.config = config;
        this.localNode = localNode;
        this.userToMessage = userToMessage;
        this.nodes = new HashMap<>();
        this.attempts = new HashMap<>();
        this.messagesInTransit = new HashMap<>();

        this.textMessage = textMessage;
        this.forwarding = forwarding;
    }

    public SendMessageOperation(Server server, LocalNode localNode, Configuration config, User userToMessage, TextMessage message, boolean forwarding) {
        this.server = server;
        this.config = config;
        this.localNode = localNode;
        this.user = userToMessage;
        this.userToMessage = userToMessage;
        this.nodes = new HashMap<>();
        this.attempts = new HashMap<>();
        this.messagesInTransit = new HashMap<>();

        this.message = message;
        this.forwarding = forwarding;

        this.message.setOrigin(localNode.getNode());
    }

    /**
     * Send the connect message to the target node and wait for a reply. Run FindNodeOperation and BucketRefreshOperation if the target node responds to populate the local RoutingTable
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {

        isMessagedSuccessfully = false;
        hasTimeoutOccurred = false;

        /* Get the user object of the user we would like to message */
        if (forwarding == false) {
            FindUserOperation fuo = new FindUserOperation(server, localNode, config, userToMessage);
            fuo.execute();
            user = fuo.getUser();
            closestNodes = fuo.getClosestNodes();
        }

        /* Add associated nodes to the 'to message' list */
        addNodes(user.getAssociatedNodes());

        /* Run the message operation for only the intended recipients to begin with */
        messageLoop();

        /* Don't terminate until all replies have either been received or have timed out */
        while (messagesInTransit.size() > 0) {
            try {
                iterativeQueryNodes();
                wait(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /* Add the next k closest nodes and run the message operation again if the node wasn't reached successfully */
        if (isMessagedSuccessfully == false && hasTimeoutOccurred == true && forwarding == false) {
            addNodes(closestNodes);
            messageLoop();
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
                nodes.put(node, NOT_QUERIED);
            }

            if (!attempts.containsKey(node)) {
                attempts.put(node, 0);
            }
        }
    }

    private void messageLoop() throws IOException {
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
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("SendMessageOperation was interrupted unexpectedly: {}", e);
        }
    }

    private boolean iterativeQueryNodes() throws IOException {
        /* Maximum number of messages already in transit */
        if (config.getMaxConcurrency() <= messagesInTransit.size()) {
            return false;
        }

        List<Node> toQuery = new ArrayList<>();

        /* Add not queried and failed nodes to the toQuery List if they haven't failed
         * getMaxConnectionAttempts() times */
        for (Map.Entry<Node, String> e : nodes.entrySet()) {
            if (e.getValue().equals(NOT_QUERIED) || e.getValue().equals(FAILED)) {
                if (attempts.get(e.getKey()) < config.getMaxConnectionAttempts()) {
                    toQuery.add(e.getKey());
                }
            }
        }

        /* No not messaged nodes nor any messages in transit - finish */
        if (toQuery.isEmpty() && messagesInTransit.isEmpty()) {
            return true;
        }

        /* Create new messages for every not queried node, not exceeding config.getMaxConcurrency() */
        for (int i = 0; (messagesInTransit.size() < config.getMaxConcurrency()) && (i < toQuery.size()); i++) {

            /* Handle a node sending a message to itself */
            if (toQuery.get(i).equals(localNode.getNode())) {
                if (forwarding == false) {
                        message = new TextMessage(localNode.getNode(), user.getAssociatedNodes().get(0), localNode.getUsers().getLocalUser(), user, textMessage);
                }
                localNode.getMessages().addReceivedMessage(message);
                isMessagedSuccessfully = true;
                nodes.put(toQuery.get(i), QUERIED);
            } else {
                if (forwarding == false) {
                    message = new TextMessage(localNode.getNode(), user.getAssociatedNodes().get(0), localNode.getUsers().getLocalUser(), user, textMessage);
                }
                int communicationId = server.sendMessage(toQuery.get(i), message, this);

                nodes.put(toQuery.get(i), AWAITING_ACK);
                attempts.put(toQuery.get(i), attempts.get(toQuery.get(i)) + 1);
                messagesInTransit.put(communicationId, toQuery.get(i));
            }
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
        /* Read the incoming AcknowledgeMessage */
        AcknowledgeMessage msg = (AcknowledgeMessage) incoming;

        logger.debug("ACK received from {}", msg.getOrigin().getSocketAddress().getHostName());

        isMessagedSuccessfully = true; // we've got an ack so the message was received

        /* Update the hashmap to show that we've finished messaging this node */
        nodes.put(msg.getOrigin(), QUERIED);

         /* Remove this msg from messagesTransiting since it's completed now */
        messagesInTransit.remove(communicationId);
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

        isMessagedSuccessfully = false;
        hasTimeoutOccurred = true;

        /* Mark this node as failed, increment attempts, remove message in transit */
        nodes.put(n, FAILED);
        attempts.put(n, attempts.get(n) + 1);
        messagesInTransit.remove(communicationId);
    }

    public synchronized boolean isMessagedSuccessfully() {
        return isMessagedSuccessfully;
    }
}

