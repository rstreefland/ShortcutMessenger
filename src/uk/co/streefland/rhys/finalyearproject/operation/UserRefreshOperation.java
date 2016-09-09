package uk.co.streefland.rhys.finalyearproject.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;

import java.io.IOException;

/**
 * Refreshes all buckets within the RoutingTable
 */
public class UserRefreshOperation implements Operation {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Server server;
    private final LocalNode localNode;
    private final Configuration config;

    public UserRefreshOperation(Server server, LocalNode localNode, Configuration config) {
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

        for (int i = 1; i < KeyId.ID_LENGTH; i++) {
            /* Generate a KeyId that is n bits away from the current nodeId */
            final KeyId current = localNode.getNode().getNodeId().generateNodeIdUsingDistance(i);

            /* Run FindNodeOperation in a different thread */
            new Thread() {
                @Override
                public void run() {
                    try {
                        new FindNodeOperation(server, localNode, current, config).execute();
                    } catch (IOException e) {
                        logger.error("Bucket refresh failed with error:", e);
                    }
                }
            }.start();
        }
    }
}
