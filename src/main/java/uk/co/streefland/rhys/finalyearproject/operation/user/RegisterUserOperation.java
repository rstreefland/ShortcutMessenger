package uk.co.streefland.rhys.finalyearproject.operation.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.message.AcknowledgeMessage;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.message.user.StoreUserMessage;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.operation.FindNodeOperation;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rhys on 03/09/2016.
 */
public class RegisterUserOperation implements Operation, Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Server server;
    private final Configuration config;
    private final LocalNode localNode;
    private final User user;

    private Message message; // Message sent to each peer
    private final Map<Node, String> nodes;
    private final Map<Node, Integer> attempts;

    /* Tracks messages in transit and awaiting reply */
    private final Map<Integer, Node> messagesInTransit;

    private boolean isRegisteredSuccessfully;
    private boolean storeUserOnLocalNode;
    private boolean ignoreStale;

    public RegisterUserOperation(LocalNode localNode, User user, boolean ignoreStale) {
        this.server = localNode.getServer();
        this.config = localNode.getConfig();
        this.localNode = localNode;
        this.user = user;
        this.nodes = new HashMap<>();
        this.attempts = new HashMap<>();
        this.messagesInTransit = new HashMap<>();
        this.ignoreStale = ignoreStale;
    }

    /**
     * Send the node message to the target node and wait for a reply. Run FindNodeOperation and BucketRefreshOperation if the target node responds to populate the local RoutingTable
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {

        isRegisteredSuccessfully = true; // true until disproved by a different node
        storeUserOnLocalNode = false;

        FindNodeOperation operation = new FindNodeOperation(localNode, user.getUserId(), ignoreStale);
        operation.execute();
        addNodes(operation.getClosestNodes());

        message = new StoreUserMessage(localNode.getNode(), user);

        try {
            /* If operation hasn't finished, wait for a maximum of config.operationTimeout() time */
            int totalTimeWaited = 0;
            int timeInterval = 10;
            while (true) {
                if (!iterativeQueryNodes()) {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                } else {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("RegisterUserOperation was interrupted unexpectedly: {}", e);
        }

        /* Add the user to our localStorage once we know that it doesn't exist already on any other nodes */
        if (storeUserOnLocalNode) {
            isRegisteredSuccessfully = localNode.getUsers().addUser(user);
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

    private boolean iterativeQueryNodes() throws IOException {
        /* Maximum number of messages already in transit */
        if (Configuration.MAX_CONCURRENCY <= messagesInTransit.size()) {
            return false;
        }

        List<Node> toQuery = new ArrayList<>();

        /* Add not queried and failed nodes to the toQuery List if they haven't failed
         * getMaxConnectionAttempts() times */
        for (Map.Entry<Node, String> e : nodes.entrySet()) {
            if (e.getValue().equals(Configuration.NOT_QUERIED) || e.getValue().equals(Configuration.FAILED)) {
                if (attempts.get(e.getKey()) < config.getMaxConnectionAttempts()) {
                    toQuery.add(e.getKey());
                }
            }
        }

        /* No not messaged nodes nor any messages in transit - finish */
        if (toQuery.isEmpty() && messagesInTransit.isEmpty()) {
            return true;
        }

        /* Create new messages for every not queried node, not exceeding Configuration.MAX_CONCURRENCY */
        for (int i = 0; (messagesInTransit.size() < Configuration.MAX_CONCURRENCY) && (i < toQuery.size()); i++) {

            Node n = toQuery.get(i);

            /* Don't message a node with the same IP address as the local node because it's stale  - mark it as unresponsive */
            if (n.getPublicInetAddress().equals(localNode.getNode().getPublicInetAddress()) && n.getPrivateInetAddress().equals(localNode.getNode().getPrivateInetAddress()) && n.getPrivatePort() == localNode.getNode().getPrivatePort()) {
                //logger.info("Not running find node operation against stale node");
                localNode.getRoutingTable().setUnresponsiveContact(n);
                nodes.put(n, Configuration.QUERIED);
            } else {

                if (n.equals(localNode.getNode())) {
                /* Can only store user on local node once we know that it doesn't already exist on another node.
                 * So, we set this flag and handle this once we know for sure that the user doesn't already exist on another node */
                    storeUserOnLocalNode = true;
                    nodes.put(n, Configuration.QUERIED);
                } else {

                    int communicationId = server.sendMessage(n, message, this);

                    nodes.put(n, Configuration.AWAITING_REPLY);
                    attempts.put(n, attempts.get(n) + 1);
                    messagesInTransit.put(communicationId, n);
                }
            }
        }
        return false;
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

        if (!msg.getOperationSuccessful()) {
            isRegisteredSuccessfully = false;
        }

        /* Update the hashmap to show that we've finished messaging this node */
        nodes.put(msg.getOrigin(), Configuration.QUERIED);

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
        nodes.put(n, Configuration.FAILED);
        attempts.put(n, attempts.get(n) + 1);
        messagesInTransit.remove(communicationId);
    }

    public synchronized boolean isRegisteredSuccessfully() {
        return isRegisteredSuccessfully;
    }
}

