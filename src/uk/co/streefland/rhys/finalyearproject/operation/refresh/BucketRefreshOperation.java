package uk.co.streefland.rhys.finalyearproject.operation.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.operation.FindNodeOperation;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;

import java.io.IOException;

/**
 * Refreshes all buckets within the RoutingTable
 */
public class BucketRefreshOperation implements Operation {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalNode localNode;

    public BucketRefreshOperation(LocalNode localNode) {
        this.localNode = localNode;
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
            final KeyId current = localNode.getNode().getNodeId().generateKeyIdUsingDistance(i);

            /* Run each FindNodeOperations in a different thread */
            new Thread() {
                @Override
                public void run() {
                    try {
                        new FindNodeOperation(localNode, current).execute();
                    } catch (IOException e) {
                        logger.error("Bucket refresh failed with error:", e);
                    }
                }
            }.start();
        }
    }
}
