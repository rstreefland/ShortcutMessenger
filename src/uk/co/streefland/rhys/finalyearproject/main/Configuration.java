package uk.co.streefland.rhys.finalyearproject.main;

import java.io.File;

/**
 * Created by Rhys on 22/08/2016.
 */
public class Configuration {
    private final static long RESTORE_INTERVAL = 60 * 1000; // in milliseconds
    private final static long RESPONSE_TIMEOUT = 2000;
    private final static long OPERATION_TIMEOUT = 2000;
    private final static int CONCURRENCY = 3;
    private final static int K = 5;
    private final static int RCSIZE = 3;
    private final static int STALE = 1;
    private final static String LOCAL_FOLDER = "kademlia";

    private final static boolean IS_TESTING = true;

    /**
     * Default constructor to support Gson Serialization
     */
    public Configuration()
    {

    }

    public long restoreInterval()
    {
        return RESTORE_INTERVAL;
    }

    public long responseTimeout()
    {
        return RESPONSE_TIMEOUT;
    }

    public long operationTimeout()
    {
        return OPERATION_TIMEOUT;
    }

    public int maxConcurrentMessagesTransiting()
    {
        return CONCURRENCY;
    }

    public int k()
    {
        return K;
    }

    public int replacementCacheSize()
    {
        return RCSIZE;
    }

    public int stale()
    {
        return STALE;
    }

    public String getNodeDataFolder(String ownerId)
    {
        /* Setup the main storage folder if it doesn't exist */
        String path = System.getProperty("user.home") + File.separator + Configuration.LOCAL_FOLDER;
        File folder = new File(path);
        if (!folder.isDirectory())
        {
            folder.mkdir();
        }

        /* Setup subfolder for this owner if it doesn't exist */
        File ownerFolder = new File(folder + File.separator + ownerId);
        if (!ownerFolder.isDirectory())
        {
            ownerFolder.mkdir();
        }

        /* Return the path */
        return ownerFolder.toString();
    }

    public boolean isTesting()
    {
        return IS_TESTING;
    }
}
