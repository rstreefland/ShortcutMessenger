package uk.co.streefland.rhys.finalyearproject.operation;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;
import uk.co.streefland.rhys.finalyearproject.message.AcknowledgeMessage;
import uk.co.streefland.rhys.finalyearproject.message.ConnectMessage;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;

/**
 * Created by Rhys on 30/08/2016.
 */
public class ConnectOperation implements Operation, Receiver {

    public static final int MAX_CONNECT_ATTEMPTS = 5;

    private final Server server;
    private final LocalNode localNode;
    private final Node bootstrapNode;
    private final Configuration config;

    private int attempts;
    private boolean error;

    /**
     * @param server    The message server used to send/receive messages
     * @param localNode     The local node
     * @param bootstrap Node to use to bootstrap the local node onto the network
     * @param config
     */
    public ConnectOperation(Server server, LocalNode localNode, Node bootstrap, Configuration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.bootstrapNode = bootstrap;
        this.config = config;
    }

    @Override
    public synchronized void execute() throws IOException
    {
        try
        {
            /* Contact the bootstrap node */
            this.error = true;
            this.attempts = 0;
            Message m = new ConnectMessage(this.localNode.getNode());

            /* Send a connect message to the bootstrap node */
            server.sendMessage(this.bootstrapNode, m, this);

            /* If we haven't finished as yet, wait for a maximum of config.operationTimeout() time */
            int totalTimeWaited = 0;
            int timeInterval = 50;     // We re-check every 300 milliseconds
            while (totalTimeWaited < this.config.operationTimeout())
            {
                if (error)
                {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                }
                else
                {
                    break;
                }
            }
            if (error)
            {
                /* If we still haven't received any responses by then, do a routing timeout */
                throw new IOException("ConnectOperation: Bootstrap node did not respond: " + bootstrapNode);
            }

            /* Perform lookup for our own ID to get nodes close to us */
           // Operation lookup = new NodeLookupOperation(this.server, this.localNode, this.localNode.getNode().getNodeId(), this.config);
           // lookup.execute();

            /**
             * Refresh buckets to get a good routing table
             * After the above lookup operation, K nodes will be in our routing table,
             * Now we try to populate all of our buckets.
             */
           // new BucketRefreshOperation(this.server, this.localNode, this.config).execute();
        }
        catch (InterruptedException e)
        {
            System.err.println("Connect operation was interrupted. ");
        }
    }

    /**
     * Receives an AcknowledgeMessage from the bootstrap node.
     *
     * @param comm
     */
    @Override
    public synchronized void receive(Message incoming, int comm)
    {
        /* The incoming message will be an acknowledgement message */
        AcknowledgeMessage msg = (AcknowledgeMessage) incoming;

        /* The bootstrap node has responded, insert it into our space */
        this.localNode.getRoutingTable().insert(this.bootstrapNode);

        /* We got a response, so the error is false */
        error = false;

        /* Wake up any waiting thread */
        notify();
    }

    @Override
    public synchronized void timeout(int comm) throws IOException {
        if (this.attempts < MAX_CONNECT_ATTEMPTS) {
            this.server.sendMessage(this.bootstrapNode, new ConnectMessage(this.localNode.getNode()), this);
        } else {
            /* We just exit, so notify all other threads that are possibly waiting */
            notify();
        }
    }

}
