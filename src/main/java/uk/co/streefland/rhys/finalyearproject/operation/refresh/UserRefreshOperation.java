package uk.co.streefland.rhys.finalyearproject.operation.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.message.content.TextMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;
import uk.co.streefland.rhys.finalyearproject.operation.SendMessageOperation;
import uk.co.streefland.rhys.finalyearproject.operation.user.LoginUserOperation;
import uk.co.streefland.rhys.finalyearproject.operation.user.RegisterUserOperation;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Updates other nodes with the users stored on the local node
 */
public class UserRefreshOperation implements Operation {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalNode localNode;

    /* Cached threadPool so we can run the operation in parallel */
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

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

        // Make sure the local user is up to date across the network
        localNode.getUsers().getLocalUser().setLastActiveTime();

        for (Map.Entry<String, User> entry : localNode.getUsers().getUsers().entrySet()) {

            /* Run each RegisterUserOperation in a different thread */
            threadPool.execute(new Thread(() -> runOperation(entry)));
        }

        threadPool.shutdown();
    }

    private void runOperation(Map.Entry<String, User> entry) {
        try {
            new RegisterUserOperation(localNode, entry.getValue(), false).execute();

        } catch (IOException e) {
            logger.error("User refresh failed with error:", e);
        }
    }
}
