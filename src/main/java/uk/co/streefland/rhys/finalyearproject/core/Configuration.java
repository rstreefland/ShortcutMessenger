package uk.co.streefland.rhys.finalyearproject.core;

import java.io.Serializable;

/**
 * Stores the configuration constants and variables for the other classes
 */
public class Configuration implements Serializable {

    /* Flags that represent node states */
    public enum Status {
        NOT_QUERIED, AWAITING_REPLY, QUERIED, FAILED
    }

    /* Constants */
    public static final int K = 5; // maximum number of contacts per bucket (kademlia specifies 20 but I'm sticking with 5 for now because it's better to unused for a small network)
    public static final int MAX_CONCURRENCY = 3; // maximum number of concurrent connection
    public static final int PACKET_SIZE = 64 * 1024;  // maximum UDP packet size = 64KB
    public static final long FORWARD_MESSAGE_EXPIRY = 172800; // time for a message to be stored before it is deleted (two days)
    public static final long USER_CACHE_EXPIRY = 172800; // time for a user object to be cached before it is deleted (two days)
    public static final int DEFAULT_PORT = 12345;
    public static final String FILE_PATH = "savedstate.ser";

    /* Settings the program can change if needs be */
    private int maxConnectionAttempts = 5;
    private long operationTimeout = 1000;  // timeout for operation completion (1 second)
    private long responseTimeout = 2000; // timeout waiting for response (2 seconds)
    private long refreshInterval = 60 * 1000; // refresh interval in milliseconds (1 minute for now)

    public int getMaxConnectionAttempts() {
        return maxConnectionAttempts;
    }

    public void setMaxConnectionAttempts(int maxConnectionAttempts) {
        this.maxConnectionAttempts = maxConnectionAttempts;
    }

    public long getOperationTimeout() {
        return operationTimeout;
    }

    public void setOperationTimeout(long operationTimeout) {
        this.operationTimeout = operationTimeout;
    }

    public long getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(long responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
