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

        /* Run BucketRefreshOperation to refresh the buckets */
        try {
            new BucketRefreshOperation(localNode).execute();
            logger.debug("Routing table was refreshed");
        } catch (IOException e) {
            logger.error("Routing table refresh failed:", e);
        }

        /* Run UserRefreshOperation to refresh the user database */
        try {
            new UserRefreshOperation(localNode).execute();
            logger.debug("User database was refreshed");
        } catch (IOException e) {
            logger.error("User database refresh failed:", e);
        }

        /* Run MessageRefreshOperation to forward messages to their intended recipients*/
        try {
            new MessageRefreshOperation(localNode).execute();
            logger.info("Messages were refreshed");
        } catch (IOException e) {
            logger.error("Message refresh failed:", e);
        }

        // TODO: 02/09/2016  we'll need to refresh any data stored in the DHT as well once that functionality has been implemented
    }
}
