package uk.co.streefland.rhys.finalyearproject.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;
import uk.co.streefland.rhys.finalyearproject.node.KeyComparator;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Refreshes all buckets within the RoutingTable
 */
public class BucketRefreshOperation implements Operation {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
                        logger.error("Bucket refresh failed with error: {}", e);
                    }
                }
            }.start();
        }
    }
}
