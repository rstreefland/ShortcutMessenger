package uk.co.streefland.rhys.finalyearproject.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;

import java.io.IOException;

/**
 * Runs each receiver task in a different thread. This is designed to stop lengthy receiver tasks from blocking the main server thread.
 */
class ReceiverTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Receiver receiver;
    private final Message msg;
    private final int communicationId;

    public ReceiverTask(Receiver receiver, Message msg, int communicationId) {
        this.receiver = receiver;
        this.msg = msg;
        this.communicationId = communicationId;
    }

    /**
     * Invokes the receiver method
     */
    public void run() {
        try {
            receiver.receive(msg, communicationId);
        } catch (IOException e) {
            logger.error("This receiver task encountered an error:", e);
        }
    }
}
