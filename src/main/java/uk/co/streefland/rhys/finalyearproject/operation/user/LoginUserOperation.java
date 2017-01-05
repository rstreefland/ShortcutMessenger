package uk.co.streefland.rhys.finalyearproject.operation.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.message.user.VerifyUserMessage;
import uk.co.streefland.rhys.finalyearproject.message.user.VerifyUserMessageReply;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.operation.FindNodeOperation;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logs in a user
 */
public class LoginUserOperation implements Operation, Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Server server;
    private final Configuration config;
    private final LocalNode localNode;
    private final User user;
    private final String plainTextPassword;
    private Message message;
    private final Map<Node, Configuration.Status> nodes;
    private final Map<Node, Integer> attempts;
    private final Map<Integer, Node> messagesInTransit;

    private boolean loggedIn;

    public LoginUserOperation(LocalNode localNode, User user, String plainTextPassword) {
        this.server = localNode.getServer();
        this.config = localNode.getConfig();
        this.localNode = localNode;
        this.user = user;
        this.plainTextPassword = plainTextPassword;
        this.nodes = new HashMap<>();
        this.attempts = new HashMap<>();
        this.messagesInTransit = new HashMap<>();
    }

    /**
     * Send the node message to the target node and wait for a reply. Run FindNodeOperation and BucketRefreshOperation if the target node responds to populate the local RoutingTable
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {
        loggedIn = false; // not logged in until another node proves otherwise

        /* Look for user on the local node first */
        User localUser = localNode.getUsers().findUser(user.getUserName());

        if (localUser != null) {
            if (localUser.doPasswordsMatch(plainTextPassword)) {
                loggedIn = true;
                return;
            }
        }

        /* Find nodes closest to the userId */
        FindNodeOperation operation = new FindNodeOperation(localNode, user.getUserId(), true);
        operation.execute();
        addNodes(operation.getClosestNodes());

        message = new VerifyUserMessage(localNode.getNetworkId(), localNode.getNode(), user);

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
            logger.error("LoginUserOperation was interrupted unexpectedly: {}", e);
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
                nodes.put(node, Configuration.Status.NOT_QUERIED);
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
        for (Map.Entry<Node, Configuration.Status> e : nodes.entrySet()) {
            if (e.getValue().equals(Configuration.Status.NOT_QUERIED) || e.getValue().equals(Configuration.Status.FAILED)) {
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
                nodes.put(n, Configuration.Status.QUERIED);
            } else {
                /* Query the local node */
                if (n.equals(localNode.getNode())) {

                    /* Handles finding the user object on the local node */
                    User existingUser = localNode.getUsers().findUser(user);

                    /* Terminate early if found on the local node */
                    if (existingUser != null) {
                        if (existingUser.doPasswordsMatch(plainTextPassword)) {
                            loggedIn = true;
                            return true;
                        } else {
                            loggedIn = false;
                            return true;
                        }
                    }

                    nodes.put(n, Configuration.Status.QUERIED);
                } else {

                    int communicationId = server.sendMessage(n, message, this);

                    nodes.put(n, Configuration.Status.AWAITING_REPLY);
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
        /* Read the VerifyUserMessageReply */
        VerifyUserMessageReply msg = (VerifyUserMessageReply) incoming;

        loggedIn = msg.getExistingUser() != null && msg.getExistingUser().doPasswordsMatch(plainTextPassword);

        /* Update the hashmap to show that we've finished messaging this node */
        nodes.put(msg.getOrigin(), Configuration.Status.QUERIED);

         /* Remove this message from messagesInTransit */
        messagesInTransit.remove(communicationId);

        /* Wake up waiting thread */
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
        nodes.put(n, Configuration.Status.FAILED);
        attempts.put(n, attempts.get(n) + 1);
        messagesInTransit.remove(communicationId);

        /* Wake up waiting thread */
        notify();
    }

    public synchronized boolean isLoggedIn() {
        return loggedIn;
    }
}

