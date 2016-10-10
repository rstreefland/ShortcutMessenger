package uk.co.streefland.rhys.finalyearproject.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.message.AcknowledgeMessage;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.message.connect.ConnectMessage;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;

/**
 * Bootstraps the LocalNode to a network by connecting it to another Node and retrieving a list of Nodes from that Node
 */
public class ConnectOperation implements Operation, Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Server server;
    private LocalNode localNode;
    private Node bootstrapNode;
    private Configuration config;

    private int attempts;
    private boolean error;

    public ConnectOperation(Server server, LocalNode localNode, Node bootstrap, Configuration config) {
        this.server = server;
        this.localNode = localNode;
        this.bootstrapNode = bootstrap;
        this.config = config;
    }

    /**
     * Send the connect message to the target node and wait for a reply.
     * Run FindNodeOperation and BucketRefreshOperation if the target node responds to populate the local RoutingTable
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {

        try {
            /* Contact the bootstrap node */
            error = true;
            attempts = 0;

            /* Construct a connect message and send it to the bootstrap node */
            Message m = new ConnectMessage(localNode.getNode());
            server.sendMessage(bootstrapNode, m, this);
            attempts++;

            /* If operation hasn't finished, wait for a maximum of config.operationTimeout() time */
            int totalTimeWaited = 0;
            int timeInterval = 50;
            while (totalTimeWaited < config.getOperationTimeout()) {
                if (error) {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                } else {
                    break;
                }
            }

            /* Replaced the exception because we don't need unnecessary exceptions */
            if (error) {
                return;
            }

            /* Perform lookup for our own ID to get the K nodes closest to LocalNode */
            Operation findNode = new FindNodeOperation(server, localNode, localNode.getNode().getNodeId(), config);
            findNode.execute();

            /* Refresh buckets to update the rest of the routing table */
            new BucketRefreshOperation(server, localNode, config).execute();

        } catch (InterruptedException e) {
            logger.error("Connect operation was interrupted: {} ", e);
        }
    }

    /**
     * Receives an AcknowledgeMessage from the target node
     *
     * @param communicationId
     */
    @Override
    public synchronized void receive(Message incoming, int communicationId) {
        /* The incoming message is an acknowledgement message */
        AcknowledgeMessage msg = (AcknowledgeMessage) incoming;

        /* Update the node so we have the correct nodeId */
        bootstrapNode = msg.getOrigin();

        /* The target node has responded, insert it into the routing table */
        localNode.getRoutingTable().insert(bootstrapNode);

        /* We got a response, so error is false */
        error = false;

        /* Wake up any waiting thread */
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
        if (attempts < config.getMaxConnectionAttempts()) {
            server.sendMessage(bootstrapNode, new ConnectMessage(localNode.getNode()), this);
            attempts++;
        } else {
            /* Do nothing, wake up any waiting thread */
            notify();
        }
    }

    public synchronized boolean isError() {
        return error;
    }
}
