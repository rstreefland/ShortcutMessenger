package uk.co.streefland.rhys.finalyearproject.operation;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;

import java.io.IOException;

/**
 * Refreshes all buckets within the RoutingTable
 */
public class BucketRefreshOperation implements Operation {

    private final Server server;
    private final LocalNode localNode;
    private final Configuration config;

    public BucketRefreshOperation(Server server, LocalNode localNode, Configuration config) {
        this.server = server;
        this.localNode = localNode;
        this.config = config;
    }

    /**
     * Refreshes each bucket in a separate thread using the FindNodeOperation.
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {

        for (int i = 1; i < NodeId.ID_LENGTH; i++) {
            /* Generate a NodeId that is n bits away from the current nodeId */
            final NodeId current = localNode.getNode().getNodeId().generateNodeIdUsingDistance(i);

            /* Run FindNodeOperation in a different thread */
            new Thread() {
                @Override
                public void run() {
                    try {
                        new FindNodeOperation(server, localNode, current, config).execute();
                    } catch (IOException e) {
                        System.err.println("Bucket Refresh Operation Failed. Msg: " + e.getMessage());
                    }
                }
            }.start();
        }
    }
}
