package uk.co.streefland.rhys.finalyearproject.operation.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.operation.FindNodeOperation;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Refreshes all buckets within the RoutingTable
 */
public class BucketRefreshOperation implements Operation {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalNode localNode;

    /* Cached threadPool so we can run the operation in parallel */
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

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
            threadPool.execute(new Thread(() -> runOperation(current)));
        }

        threadPool.shutdown();
    }

    private void runOperation(KeyId current) {
        try {
            new FindNodeOperation(localNode, current, false).execute();
        } catch (IOException e) {
            logger.error("Bucket refresh failed with error:", e);
        }
    }
}
