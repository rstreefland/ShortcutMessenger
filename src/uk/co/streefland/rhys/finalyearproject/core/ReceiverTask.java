package uk.co.streefland.rhys.finalyearproject.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;

import java.io.IOException;

/**
 * Created by Rhys on 10/09/2016.
 */
public class ReceiverTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    Receiver receiver;
    Message msg;
    int communicationId;

    public ReceiverTask(Receiver receiver, Message msg, int communicationId) {

        this.receiver = receiver;
        this.msg = msg;
        this.communicationId = communicationId;
    }

    public void run() {
        try {
            receiver.receive(msg, communicationId);
        } catch (IOException e) {
            logger.error("This receiver task encountered an error:", e);
        }
    }
}
