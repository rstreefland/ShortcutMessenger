package uk.co.streefland.rhys.finalyearproject.operation;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;

import java.io.IOException;

/**
 * At each time interval t, nodes need to refresh their K-Buckets
 * This operation takes care of refreshing this node's K-Buckets
 *
 * @author Joshua Kissoon
 * @created 20140224
 */
public class BucketRefreshOperation implements Operation
{

    private final Server server;
    private final LocalNode localNode;
    private final Configuration config;

    public BucketRefreshOperation(Server server, LocalNode localNode, Configuration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.config = config;
    }

    /**
     * Each bucket need to be refreshed at every time interval t.
     * Find an identifier in each bucket's range, use it to look for nodes closest to this identifier
     * allowing the bucket to be refreshed.
     *
     * Then Do a NodeLookupOperation for each of the generated NodeIds,
     * This will find the K-Closest nodes to that ID, and update the necessary K-Bucket
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException
    {

        for (int i = 1; i < NodeId.ID_LENGTH; i++)
        {
            // Construct a NodeId that is i bits away from the current node Id
            final NodeId current = this.localNode.getNode().getNodeId().generateNodeIdUsingDistance(i);

            // Run the Node Lookup Operation, each in a different thread to speed up things
            new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        new FindNodeOperation(server, localNode, current, BucketRefreshOperation.this.config).execute();
                    }
                    catch (IOException e)
                    {
                        System.err.println("Bucket Refresh Operation Failed. Msg: " + e.getMessage());
                    }
                }
            }.start();


        }
    }
}
