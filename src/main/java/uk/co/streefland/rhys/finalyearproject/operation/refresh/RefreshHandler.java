package uk.co.streefland.rhys.finalyearproject.operation.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;

import java.io.IOException;
import java.util.TimerTask;

/**
 * Handles refreshing the buckets stored in the routing table. Will eventually also handle refreshing the data stored in the DHT
 */
public class RefreshHandler extends TimerTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalNode localNode;

    public RefreshHandler(LocalNode localNode) {
        this.localNode = localNode;
    }

    @Override
    public void run() {

        long start = System.currentTimeMillis();

        /* Run BucketRefreshOperation to refresh the buckets */
        if (localNode.getNode() != null) {
            try {
                new BucketRefreshOperation(localNode).execute();
                logger.debug("Routing table was refreshed");
            } catch (IOException e) {
                logger.error("Routing table refresh failed:", e);
            }
        }

        /* Run UserRefreshOperation to refresh the user database */
        if (localNode.getUsers().getLocalUser() != null) {
            try {
                new UserRefreshOperation(localNode).execute();
                logger.debug("User database was refreshed");
            } catch (IOException e) {
                logger.error("User database refresh failed:", e);
            }
        }

        /* Run MessageRefreshOperation to forward messages to their intended recipients*/
        try {
            new MessageRefreshOperation(localNode).execute();
            logger.debug("Messages were refreshed");
        } catch (IOException e) {
            logger.error("Message refresh failed:", e);
        }

        long end = System.currentTimeMillis();
        long timeTakenSeconds = (end-start)/1000;

        logger.info("Refresh operations took " + timeTakenSeconds + " seconds");
    }
}
