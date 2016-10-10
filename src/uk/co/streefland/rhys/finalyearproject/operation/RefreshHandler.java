package uk.co.streefland.rhys.finalyearproject.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;

import java.io.IOException;
import java.util.TimerTask;

/**
 * Handles refreshing the buckets stored in the routing table. Will eventually also handle refreshing the data stored in the DHT
 */
public class RefreshHandler extends TimerTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Server server;
    private final LocalNode localNode;
    private final Configuration config;

    public RefreshHandler(Server server, LocalNode localNode, Configuration config) {
        this.server = server;
        this.localNode = localNode;
        this.config = config;
    }

    @Override
    public void run() {
        /* Run BucketRefreshOperation to refresh the buckets */
        try {
            new BucketRefreshOperation(server, localNode, config).execute();
            logger.debug("Routing table was refreshed");
        } catch (IOException e) {
            logger.error("Routing table refresh failed:", e);
        }

        /* Run UserRefreshOperation to refresh the user database */
        try {
            new UserRefreshOperation(server, localNode, config).execute();
            logger.debug("User database was refreshed");
        } catch (IOException e) {
            logger.error("User database refresh failed:", e);
        }

        /* Run MessageRefreshOperation to forward messages*/
        try {
            new MessageRefreshOperation(server, localNode, config).execute();
            logger.info("Messages were refreshed");
        } catch (IOException e) {
            logger.error("Message refresh failed:", e);
        }

        // TODO: 02/09/2016  we'll need to refresh any data stored in the DHT as well once that functionality has been implemented
    }
}
