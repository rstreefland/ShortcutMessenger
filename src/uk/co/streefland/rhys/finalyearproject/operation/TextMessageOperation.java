package uk.co.streefland.rhys.finalyearproject.operation;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;
import uk.co.streefland.rhys.finalyearproject.message.*;
import uk.co.streefland.rhys.finalyearproject.node.KeyComparator;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.util.*;

/**
 * Created by Rhys on 03/09/2016.
 */
public class TextMessageOperation implements Operation, Receiver {

    // Flags that represent Node state
    private static final String NOT_QUERIED = "1";
    private static final String AWAITING_RESPONSE = "2";
    private static final String QUERIED = "3";
    private static final String FAILED = "4";

    private Server server;
    private LocalNode localNode;
    private Configuration config;

    private final Message message;        // Message sent to each peer
    private List<Node> nodes;

    /* Tracks messages in transit and awaiting reply */
    //private final Map<Integer, Node> messagesInTransit;

    /* Used to sort nodes */
    //private final Comparator comparator;

    public TextMessageOperation(Server server, LocalNode localNode, Configuration config, String message) {
        this.server = server;
        this.localNode = localNode;
        this.config = config;

        this.message = new TextMessage(localNode.getNode(), message);

       // this.messagesInTransit = new HashMap<>();

    }

    /**
     * Send the connect message to the target node and wait for a reply. Run FindNodeOperation and BucketRefreshOperation if the target node responds to populate the local RoutingTable
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {
        try {
            /* Set the local node as already asked */
            //nodes.put(localNode.getNode(), QUERIED);

            nodes = localNode.getRoutingTable().getAllNodes();

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

            /* Now after we've finished, we would have an idea of offline nodes, lets update our routing table */
            //localNode.getRoutingTable().setUnresponsiveContacts(getFailedNodes());

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean iterativeMessagesNodes() throws IOException {
        /* Maximum number of messages already in transit */
        //if (config.getMaxConcurrency() <= messagesInTransit.size()) {
        //    return false;
        //}

        //List<Node> notQueried = getClosestNodes(NOT_QUERIED);

        /* No not queried nodes nor any messages in transit - finish */
       // if (notQueried.isEmpty() && messagesInTransit.isEmpty()) {
        //    return true;
        //}

        /* Create new messages for every not queried node, not exceeding config.getMaxConcurrency() */
        //for (int i = 0; (messagesInTransit.size() < config.getMaxConcurrency()) && (i < notQueried.size()); i++) {
        for (int i = 0; i < nodes.size(); i++) {

            int communicationId = server.sendMessage(nodes.get(i), message, this);

            //nodes.put(n, AWAITING_RESPONSE);
            //messagesInTransit.put(communicationId, n);
        }
        return true;
    }


    /**
     * Receives an AcknowledgeConnectMessage from the target node
     *
     * @param communicationId
     */
    @Override
    public synchronized void receive(Message incoming, int communicationId) {
        // sweet fa at the moment

        notify();
    }

    /**
     * Runs if the two second timeout occurs. Sends the message again if attempts < maxConnectionAttempts (5)
     *
     * @param communicationId
     * @throws IOException
     */
    @Override
    public synchronized void timeout(int communicationId) throws IOException {
        /* If our attempts are less than the maxConnectionAttempts setting - try to send the message again */
       // if (attempts < config.getMaxConnectionAttempts()) {
        //    server.sendMessage(bootstrapNode, new ConnectMessage(localNode.getNode()), this);
       // } else {
        //    /* Do nothing, wake up any waiting thread */
            notify();
       // }
    }
}

