package uk.co.streefland.rhys.finalyearproject.operation;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;

import java.io.IOException;
import java.util.TimerTask;

/**
 * Handles refreshing the buckets stored in the routing table. Will eventually also handle refreshing the data stored in the DHT
 */
public class RefreshHandler extends TimerTask {

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
            System.out.println("I have REFRESHED!");
            new BucketRefreshOperation(this.server, this.localNode, this.config).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: 02/09/2016  we'll need to refresh any data stored in the DHT as well once that functionality has been implemented
    }
}
