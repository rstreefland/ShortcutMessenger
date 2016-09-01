package uk.co.streefland.rhys.finalyearproject.message;

import java.io.IOException;

/**
 * Interface for the different receivers
 */
public interface Receiver {

    /**
     * Handle incoming message
     *
     * @param communicationId The ID of this conversation, used for further conversations
     * @param incoming        The incoming message
     * @throws IOException
     */
    void receive(Message incoming, int communicationId) throws IOException;

    /**
     * If no reply is received in 2 seconds, the Server calls this method
     *
     * @param communicationId The conversation ID of this communication
     * @throws IOException
     */
    void timeout(int communicationId) throws IOException;
}
