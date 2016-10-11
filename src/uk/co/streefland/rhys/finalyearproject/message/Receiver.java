package uk.co.streefland.rhys.finalyearproject.message;

import java.io.IOException;

/**
 * Interface that every Receiver should implement to ensure consistency
 */
public interface Receiver {

    /**
     * Handle incoming message
     *
     * @param communicationId Used for replies
     * @param incoming        The incoming message
     * @throws IOException
     */
    void receive(Message incoming, int communicationId) throws IOException;

    /**
     * If no reply is received in 2 seconds, the Server calls this method
     *
     * @param communicationId
     * @throws IOException
     */
    void timeout(int communicationId) throws IOException;
}
