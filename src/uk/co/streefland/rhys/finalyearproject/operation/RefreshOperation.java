package uk.co.streefland.rhys.finalyearproject.operation;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.main.LocalNode;
import uk.co.streefland.rhys.finalyearproject.main.Server;

import java.io.IOException;
import java.util.TimerTask;

/**
 * An operation that handles refreshing the entire Kademlia Systems including buckets and content
 *
 * @author Joshua Kissoon
 * @since 20140306
 */
public class RefreshOperation extends TimerTask
{

    private final Server server;
    private final LocalNode localNode;
    //private final KademliaDHT dht;
    private final Configuration config;

    public RefreshOperation(Server server, LocalNode localNode, Configuration config)
    {
        this.server = server;
        this.localNode = localNode;
        //this.dht = dht;
        this.config = config;
    }

    @Override
    public void run()
    {
        /* Run our BucketRefreshOperation to refresh buckets */
        try {
            System.out.println("I have REFRESHED!");
            new BucketRefreshOperation(this.server, this.localNode, this.config).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* After buckets have been refreshed, we refresh content */
        //new ContentRefreshOperation(this.server, this.localNode, this.dht, this.config).execute();
    }
}
