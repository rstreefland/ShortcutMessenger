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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Pack200;

/**
 * Sends a message to another user by sending to all of the nodes associated with that user.
 * If it fails to send to an associated node then it caches the message on nodes nearby to the target node
 */
public class SendMessageOperation implements Operation, Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public final static int PENDING_DELIVERY = 1;
    public final static int DELIVERED = 2;
    public final static int PENDING_FORWARDING = 3;
    public final static int FORWARDED = 4;
    public final static int FAILED = 5;

    private final Server server;
    private final Configuration config;
    private final LocalNode localNode;
    private User user;

    private KeyId messageId;
    private String messageString;
    private long createdTime;
    private TextMessage message; // Message sent to each peer
    private final Map<Node, String> nodes;
    private final Map<Node, Integer> attempts;

    private final Map<Integer, Node> messagesInTransit;
    private final boolean forwarding;
    private int messageStatus;

    private List<Node> closestNodes;

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

        messageStatus = SendMessageOperation.PENDING_DELIVERY;

        /* Get the user object of the user we would like to message */
        if (!forwarding) {
            FindUserOperation fuo = new FindUserOperation(localNode, user);
            fuo.execute();

            user = fuo.getFoundUser();
            if (user == null) {
                return;
            }

            closestNodes = fuo.getClosestNodes();

            /* Add associated nodes to the 'to message' list */
            if (!user.getAssociatedNodes().isEmpty()) {

                Contact associatedContact = localNode.getRoutingTable().getContact(user.getAssociatedNodes().get(0));

                if (associatedContact == null) {
                    localNode.getRoutingTable().insert(user.getAssociatedNodes().get(0));
                    associatedContact = localNode.getRoutingTable().getContact(user.getAssociatedNodes().get(0));
                }

                if (associatedContact.getStaleCount() == 0) {
                    addNodes(user.getAssociatedNodes());

                    /* Run the message operation for only the intended recipients to begin with */
                    messageLoop();
                }

            } else {
                logger.info("User has no associated nodes - caching message on closest nodes");
            }

        } else {
            /* Run the message operation for the forwarded message */
            addNodes(user.getAssociatedNodes());
            messageLoop();
        }

        /* Add the next k closest nodes and run the message operation again if the node wasn't reached successfully */
        if ((messageStatus == SendMessageOperation.PENDING_DELIVERY) && !forwarding) {

            messageStatus = SendMessageOperation.PENDING_FORWARDING;

            /* Set the contact as unresponsive */
            localNode.getRoutingTable().setUnresponsiveContact(user.getAssociatedNodes().get(0));

            if (closestNodes == null) {

                /* look on the local node first */
                closestNodes = localNode.getRoutingTable().findClosest(user.getAssociatedNodes().get(0).getNodeId(), true);

                /* then find node operation if the list is still empty */
                if (closestNodes.size() == 0) {
                    FindNodeOperation fno = new FindNodeOperation(localNode, user.getAssociatedNodes().get(0).getNodeId(), true);
                    fno.execute();
                    closestNodes = fno.getClosestNodes();
                }
            }

            addNodes(closestNodes);
            messageLoop();
        }

        if (messageStatus == SendMessageOperation.PENDING_FORWARDING) {
            messageStatus = SendMessageOperation.FAILED;
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
                nodes.putIfAbsent(node, Configuration.NOT_QUERIED);
            }

            if (!attempts.containsKey(node)) {
                attempts.putIfAbsent(node, 0);
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

            if (n.getPublicInetAddress().equals(localNode.getNode().getPublicInetAddress()) && n.getPrivateInetAddress().equals(localNode.getNode().getPrivateInetAddress()) && n.getPrivatePort() == localNode.getNode().getPrivatePort()) {
                //logger.info("Not running find node operation against stale node");
                localNode.getRoutingTable().setUnresponsiveContact(n);
                nodes.put(n, Configuration.QUERIED);
            } else {

                /* Handle a node sending a message to itself */
                if (n.equals(localNode.getNode())) {

                    /* Don't message yourself, this is a message for another user */
                    if (!user.getUserId().equals(localNode.getUsers().getLocalUser().getUserId())) {
                        message = new TextMessage(localNode.getNetworkId(), messageId, localNode.getNode(), user.getAssociatedNodes().get(0), localNode.getUsers().getLocalUser(), user, messageString, createdTime);
                        localNode.getMessages().addForwardMessage(message);
                    } else {
                        /* this is a message for yourself */
                        message = new TextMessage(localNode.getNetworkId(), messageId, localNode.getNode(), user, messageString, createdTime);
                        localNode.getMessages().addReceivedMessage(message);
                        messageStatus = SendMessageOperation.DELIVERED;
                    }

                    nodes.put(n, Configuration.QUERIED);
                } else {
                    if (!forwarding) {
                        message = new TextMessage(localNode.getNetworkId(), messageId, localNode.getNode(), user.getAssociatedNodes().get(0), localNode.getUsers().getLocalUser(), user, messageString, createdTime);
                    }
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

        if (msg.getOperationSuccessful()) {

            if (messageStatus == SendMessageOperation.PENDING_DELIVERY) {
                messageStatus = SendMessageOperation.DELIVERED;
            }

            if (messageStatus == SendMessageOperation.PENDING_FORWARDING) {
                messageStatus = SendMessageOperation.FORWARDED;
            }
        }

        /* Update the hashmap to show that we've finished messaging this node */
        nodes.put(msg.getSource(), Configuration.QUERIED);

         /* Remove this msg from messagesTransiting since it's completed now */
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
        nodes.put(n, Configuration.FAILED);
        attempts.put(n, attempts.get(n) + 1);
        messagesInTransit.remove(communicationId);

        /* Wake up waiting thread */
        notify();
    }

    public synchronized int messageStatus() {
        return messageStatus;
    }

    public TextMessage getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}

