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
import uk.co.streefland.rhys.finalyearproject.message.content.TextMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.operation.user.FindUserOperation;
import uk.co.streefland.rhys.finalyearproject.routing.Contact;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends a message to another user by sending to all of the nodes associated with that user.
 * If it fails to send to an associated node then it caches the message on nodes nearby to the target node
 */
public class SendMessageOperation implements Operation, Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public enum Status {
        PENDING_DELIVERY, DELIVERED, PENDING_FORWARDING, FORWARDED, FAILED
    }

    private final Server server;
    private final Configuration config;
    private final LocalNode localNode;
    private User user;
    private List<Node> closestNodes;

    private KeyId messageId;
    private String messageString;
    private long createdTime;
    private TextMessage message; // Message sent to each peer

    private final Map<Node, Configuration.Status> nodes;
    private final Map<Node, Integer> attempts;
    private final Map<Integer, Node> messagesInTransit;
    private final boolean forwarding;
    private Status messageStatus;

    /**
     * Default constructor
     */
    public SendMessageOperation(LocalNode localNode, User userToMessage, KeyId messageId, String messageString, long createdTime) {
        this.server = localNode.getServer();
        this.config = localNode.getConfig();
        this.localNode = localNode;
        this.user = userToMessage;
        this.nodes = new HashMap<>();
        this.attempts = new HashMap<>();
        this.messagesInTransit = new HashMap<>();

        this.messageId = messageId;
        this.messageString = messageString;
        this.createdTime = createdTime;
        this.forwarding = false;
    }

    /**
     * This constructor is used to forward messages
     */
    public SendMessageOperation(LocalNode localNode, User userToMessage, TextMessage message) {
        this.server = localNode.getServer();
        this.config = localNode.getConfig();
        this.localNode = localNode;
        this.user = userToMessage;
        this.nodes = new HashMap<>();
        this.attempts = new HashMap<>();
        this.messagesInTransit = new HashMap<>();

        this.message = message;
        this.forwarding = true;

        this.message.setSource(localNode.getNode());
    }

    /**
     * Send the node message to the target node and wait for a reply. Run FindNodeOperation and BucketRefreshOperation if the target node responds to populate the local RoutingTable
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {
        messageStatus = Status.PENDING_DELIVERY;

        /* Get the user object of the user we would like to message */
        if (!forwarding) {
            FindUserOperation fuo = new FindUserOperation(localNode, user);
            fuo.execute();

            user = fuo.getFoundUser();
            if (user == null) {
                return;
            }

            closestNodes = fuo.getClosestNodes();
        } else {
            /* Don't do the find user operation if forwarding. */
            User storedUser;
            storedUser = localNode.getUsers().findUser(user);

            if (storedUser != null) {
                if (storedUser.getLastActiveTime() > user.getLastActiveTime()) {
                    user = storedUser;
                }
            }
        }

        /* Add associated nodes to the 'to message' list */
        if (user.getAssociatedNode() != null) {

            Contact associatedContact = localNode.getRoutingTable().getContact(user.getAssociatedNode());

            if (associatedContact == null) {
                localNode.getRoutingTable().insert(user.getAssociatedNode());
                associatedContact = localNode.getRoutingTable().getContact(user.getAssociatedNode());
            }

            /* Ignore the stale count if the message is being forwarded because the target node may have just come back online */
            if (associatedContact.getStaleCount() == 0 || forwarding) {
                addNode(user.getAssociatedNode());

                long start = System.currentTimeMillis();
                /* Run the message operation for only the intended recipients to begin with */
                messageLoop();

                long end = System.currentTimeMillis();
                long time = end - start;
                logger.info("DIRECT MESSSAGE LOOP TIME: " + time);
            }

        } else {
            logger.info("User has no associated nodes - caching message on closest nodes");
        }

        /* Add the next k closest nodes and run the message operation again if the node wasn't reached successfully */
        if ((messageStatus == Status.PENDING_DELIVERY) && !forwarding) {

            messageStatus = Status.PENDING_FORWARDING;

            /* Set the contact as unresponsive */
            localNode.getRoutingTable().setUnresponsiveContact(user.getAssociatedNode());

            if (closestNodes == null) {
                /* look on the local node first */
                closestNodes = localNode.getRoutingTable().findClosest(user.getAssociatedNode().getNodeId(), true);

                /* then find node operation if the list is still empty */
                if (closestNodes.size() == 0) {
                    logger.info("SMO COULDN'T FIND ANY CLOSE NODES - LOOKING ELSEWHERE");
                    FindNodeOperation fno = new FindNodeOperation(localNode, user.getAssociatedNode().getNodeId(), false); // TODO: 15/02/2017  check if this breaks anything 
                    fno.execute();
                    closestNodes = fno.getClosestNodes();
                }
            }

            addNodes(closestNodes);

            long start = System.currentTimeMillis();
            messageLoop();
            long end = System.currentTimeMillis();
            long time = end - start;
            logger.info("FORWARD MESSAGE LOOP TIME: " + time);
        }

        if (messageStatus == Status.PENDING_FORWARDING) {
            messageStatus = Status.FAILED;
        }
    }

    /**
     * Inserts the nodes into the HashMap if they're not already present
     *
     * @param list The list of nodes to insert
     */
    private void addNodes(List<Node> list) {
        for (Node n : list) {
            addNode(n);
        }
    }

    /**
     * Inserts a single node into the HashMap
     */
    private void addNode(Node n) {
        if (!nodes.containsKey(n)) {
            nodes.putIfAbsent(n, Configuration.Status.NOT_QUERIED);
        }

        if (!attempts.containsKey(n)) {
            attempts.putIfAbsent(n, 0);
        }
    }

    private void messageLoop() throws IOException {
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
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("SendMessageOperation was interrupted unexpectedly: {}", e);
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
            /* if (n.getPublicInetAddress().equals(localNode.getNode().getPublicInetAddress()) && n.getPrivateInetAddress().equals(localNode.getNode().getPrivateInetAddress()) && n.getPrivatePort() == localNode.getNode().getPrivatePort()) {
                //logger.info("Not running find node operation against stale node");
                localNode.getRoutingTable().setUnresponsiveContact(n);
                nodes.put(n, Configuration.Status.QUERIED); */

            /* Handle a node sending a message to itself */
            if (n.getPublicInetAddress().equals(localNode.getNode().getPublicInetAddress()) && n.getPrivateInetAddress().equals(localNode.getNode().getPrivateInetAddress())) {
                    /* Don't message yourself, this is a message for another user */
                if (!user.getUserId().equals(localNode.getUsers().getLocalUser().getUserId())) {
                    if (!forwarding) {
                        message = new TextMessage(localNode, messageId, user.getAssociatedNode(), user, messageString, createdTime);
                        localNode.getMessages().addForwardMessage(message);
                        nodes.put(n, Configuration.Status.QUERIED);
                        localNode.getRoutingTable().setUnresponsiveContact(n);
                    }
                }
            } else {
                if (!forwarding) {
                    message = new TextMessage(localNode, messageId, user.getAssociatedNode(), user, messageString, createdTime);
                }
                int communicationId = server.sendMessage(n, message, this);

                nodes.put(n, Configuration.Status.AWAITING_REPLY);
                attempts.put(n, attempts.get(n) + 1);
                messagesInTransit.put(communicationId, n);
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

        if (msg.getOperationSuccessful()) {

            if (messageStatus == Status.PENDING_DELIVERY) {
                messageStatus = Status.DELIVERED;
            }

            if (messageStatus == Status.PENDING_FORWARDING) {
                messageStatus = Status.FORWARDED;
            }
        }

        /* Update the hashmap to show that we've finished messaging this node */
        nodes.put(msg.getSource(), Configuration.Status.QUERIED);

         /* Remove the messageInTransit */
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

    public synchronized Status messageStatus() {
        return messageStatus;
    }

    public TextMessage getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}

