package uk.co.streefland.rhys.finalyearproject.operation.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;
import uk.co.streefland.rhys.finalyearproject.operation.user.RegisterUserOperation;

import java.io.IOException;

/**
 * Updates other nodes with the users stored on the local node
 */
public class UserRefreshOperation implements Operation {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalNode localNode;

    public UserRefreshOperation(LocalNode localNode) {
        this.localNode = localNode;
    }

    /**
     * Run the RegisterUserOperation for each user account in a different thread
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
                        new RegisterUserOperation(localNode, currentUser, false).execute();

                    } catch (IOException e) {
                        logger.error("User refresh failed with error:", e);
                    }
                }
            }.start();
        }
    }
}
