package uk.co.streefland.rhys.finalyearproject.operation.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;
import uk.co.streefland.rhys.finalyearproject.operation.user.LoginUserOperation;
import uk.co.streefland.rhys.finalyearproject.operation.user.RegisterUserOperation;

import java.io.IOException;
import java.util.Map;

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

        /* Clean up users */
        localNode.getUsers().cleanUp();

        if (localNode.getUsers().getLocalUser() != null) {
            localNode.getUsers().getLocalUser().setLastActiveTime();
            new RegisterUserOperation(localNode, localNode.getUsers().getLocalUser(), false);
        }

        for (Map.Entry<String, User> entry : localNode.getUsers().getUsers().entrySet()) {
            /* Run RegisterUserOperation for each user in a different thread */
            new Thread() {
                @Override
                public void run() {
                    try {
                        new RegisterUserOperation(localNode, entry.getValue(), false).execute();

                    } catch (IOException e) {
                        logger.error("User refresh failed with error:", e);
                    }
                }
            }.start();
        }
    }
}
