package uk.co.streefland.rhys.finalyearproject.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.message.*;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rhys on 03/09/2016.
 */
public class FindUserOperation implements Operation, Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /* Flags that represent Node states */
    private static final String NOT_QUERIED = "1";
    private static final String AWAITING_ACK = "2";
    private static final String QUERIED = "3";
    private static final String FAILED = "4";

    private Server server;
    private Configuration config;
    private LocalNode localNode;
    private User userToFind;
    private User user;


    private Message message; // Message sent to each peer
    private Map<Node, String> nodes;
    private Map<Node, Integer> attempts;

    /* Tracks messages in transit and awaiting reply */
    private Map<Integer, Node> messagesInTransit;

    public FindUserOperation(Server server, LocalNode localNode, Configuration config, User userToFind) {
        this.server = server;
        this.config = config;
        this.localNode = localNode;
        this.nodes = new HashMap<>();
        this.attempts = new HashMap<>();
        this.messagesInTransit = new HashMap<>();
        this.userToFind = userToFind;
    }

    /**
     * Send the connect message to the target node and wait for a reply. Run FindNodeOperation and BucketRefreshOperation if the target node responds to populate the local RoutingTable
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {

        user = localNode.getUsers().findUser(userToFind.getUserName()); // look for the user on the local node

        if (user != null) {
            /* We've found the user on the local node - no need to do anything else */
            return;
        }

        FindNodeOperation operation = new FindNodeOperation(server, localNode, userToFind.getUserId(), config);
        operation.execute();
        addNodes(operation.getClosestNodes());

        message = new VerifyUserMessage(localNode.getNode(), user, false);

        try {
            /* If operation hasn't finished, wait for a maximum of config.operationTimeout() time */
            int totalTimeWaited = 0;
            int timeInterval = 10;
            while ((totalTimeWaited < config.getOperationTimeout()) && user == null) {
                if (!iterativeQueryNodes()) {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                } else {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("FindUserOperation was interrupted unexpectedly: {}", e);
        }

        /* Don't terminate until we've found a user object */
        while (user == null) {
            try {
                iterativeQueryNodes();
                wait(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
            int communicationId = server.sendMessage(toQuery.get(i), message, this);

            nodes.put(toQuery.get(i), AWAITING_ACK);
            attempts.put(toQuery.get(i), attempts.get(toQuery.get(i)) + 1);
            messagesInTransit.put(communicationId, toQuery.get(i));
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
        VerifyUserReplyMessage msg = (VerifyUserReplyMessage) incoming;

        logger.debug("ACK received from {}", msg.getOrigin().getSocketAddress().getHostName());

        if (msg.getExistingUser() != null) {
            user = msg.getExistingUser();
        }

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

        /* Mark this node as failed, increment attempts, remove message in transit */
        nodes.put(n, FAILED);
        attempts.put(n, attempts.get(n) + 1);
        messagesInTransit.remove(communicationId);
    }

    public User getUser() {
        return user;
    }
}

