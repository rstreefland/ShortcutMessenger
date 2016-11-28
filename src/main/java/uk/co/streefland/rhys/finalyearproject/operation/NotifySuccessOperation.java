package uk.co.streefland.rhys.finalyearproject.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.message.AcknowledgeMessage;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.message.content.NotifySuccessMessage;
import uk.co.streefland.rhys.finalyearproject.message.node.ConnectMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.operation.refresh.BucketRefreshOperation;

import java.io.IOException;

/**
 * Bootstraps the LocalNode to a network by connecting it to another Node and retrieving a list of Nodes from that Node
 */
public class NotifySuccessOperation implements Operation, Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Server server;
    private final LocalNode localNode;
    private final Node target;
    private final Configuration config;
    private final Message message;

    private int attempts;
    private boolean error;

    public NotifySuccessOperation(Server server, LocalNode localNode, Node target, KeyId messageId, Configuration config) {
        this.server = server;
        this.localNode = localNode;
        this.target = target;
        this.config = config;

        message = new NotifySuccessMessage(localNode.getNode(), messageId);
    }

    /**
     * Send the node message to the target node and wait for a reply.
     * Run FindNodeOperation and BucketRefreshOperation if the target node responds to populate the local RoutingTable
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {

        try {
            error = true;
            attempts = 0;

            server.sendMessage(target, message, this);
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
        error = false;

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
    public synchronized void timeout(int communicationId) throws IOException {
        /* If our attempts are less than the maxConnectionAttempts setting - try to send the message again */
        if (attempts < config.getMaxConnectionAttempts()) {
            server.sendMessage(target, message, this);
            attempts++;
        } else {
            /* Do nothing, wake up waiting thread */
            notify();
        }
    }

    public synchronized boolean isError() {
        return error;
    }
}