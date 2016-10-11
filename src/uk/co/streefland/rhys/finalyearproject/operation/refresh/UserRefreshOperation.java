package uk.co.streefland.rhys.finalyearproject.operation.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;
import uk.co.streefland.rhys.finalyearproject.operation.RegisterUserOperation;

import java.io.IOException;

/**
 * Refreshes all buckets within the RoutingTable
 */
public class UserRefreshOperation implements Operation {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LocalNode localNode;

    public UserRefreshOperation(LocalNode localNode) {
        this.localNode = localNode;
    }

    /**
     * Refreshes each bucket in a separate thread using the FindNodeOperation.
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {
        for (User currentUser : localNode.getUsers().getUsers()) {
            /* Run RegisterUserOperation for each user in a different thread */
            new Thread() {
                @Override
                public void run() {
                    try {
                        new RegisterUserOperation(localNode, currentUser).execute();

                    } catch (IOException e) {
                        logger.error("User refresh failed with error:", e);
                    }
                }
            }.start();
        }
    }
}
