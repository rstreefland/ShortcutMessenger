package uk.co.streefland.rhys.finalyearproject.message;

import java.io.IOException;

/**
 * Interface that every Receiver should implement to ensure consistency
 */
public interface Receiver {

    /**
     * Handle incoming message
     *
     * @param communicationId The communicationId, used for further communications
     * @param incoming        The incoming message
     * @throws IOException
     */
    void receive(Message incoming, int communicationId) throws IOException;

    /**
     * If no reply is received in 2 seconds, the Server calls this method
     *
     * @param communicationId The communicationId of this communication
     * @throws IOException
     */
    void timeout(int communicationId) throws IOException;
}
