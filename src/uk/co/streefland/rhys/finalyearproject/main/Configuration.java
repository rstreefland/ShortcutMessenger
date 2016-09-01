package uk.co.streefland.rhys.finalyearproject.main;

/**
 * Stores the configuration constants and variables for the other classes
 */
public class Configuration {
    /* Constants */
    private final int K = 5; // maximum number of contacts per bucket
    private final int MAX_CONCURRENCY = 3; // maximum number of concurrent connection
    private final int PACKET_SIZE = 64 * 1024;  // maximum UDP packet size = 64KB

    /* Settings the program can change if needs be */
    private long operationTimeout = 2000;  // timeout for operation completion
    private long responseTimeout = 2000; // timeout waiting for response
    private long refreshInterval = 60 * 1000; // refresh interval in milliseconds

    public int getK() {
        return K;
    }

    public int getMaxConcurrency() {
        return MAX_CONCURRENCY;
    }

    public int getPacketSize() {
        return PACKET_SIZE;
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
