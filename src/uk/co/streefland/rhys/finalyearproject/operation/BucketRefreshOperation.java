package uk.co.streefland.rhys.finalyearproject.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.core.User;

import java.io.IOException;

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

        for (User currentUser : localNode.getUsers().getUsers()) {
            /* Run RegisterUserOperation in a different thread */
            new Thread() {
                @Override
                public void run() {
                    try {
                        new RegisterUserOperation(server, localNode, config, currentUser).execute();

                    } catch (IOException e) {
                        logger.error("User refresh failed with error:", e);
                    }
                }
            }.start();
        }
    }
}
