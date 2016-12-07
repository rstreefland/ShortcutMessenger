package uk.co.streefland.rhys.finalyearproject.operation.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.message.content.TextMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.operation.FindNodeOperation;
import uk.co.streefland.rhys.finalyearproject.operation.NotifySuccessOperation;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;
import uk.co.streefland.rhys.finalyearproject.operation.SendMessageOperation;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Forwards messages to their intended recipients
 */
public class MessageRefreshOperation implements Operation {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalNode localNode;

    /* Cached threadPool so we can run the operation in parallel */
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public MessageRefreshOperation(LocalNode localNode) {
        this.localNode = localNode;
    }

    /**
     * Sends each message in a separate thread using the SendMessageOperation
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {
        /* Clean up messages */
        localNode.getMessages().cleanUp();

        for (Map.Entry<KeyId, TextMessage> entry : localNode.getMessages().getForwardMessages().entrySet()) {
            /* Run each SendMessageOperation in a different thread */
            threadPool.execute(new Thread(() -> runOperation(entry)));
        }

        threadPool.shutdown();
    }

    private void runOperation(Map.Entry<KeyId, TextMessage> entry) {
        try {
            SendMessageOperation smo = new SendMessageOperation(localNode, entry.getValue().getRecipientUser(), entry.getValue());
            smo.execute();
            if (smo.messageStatus() == SendMessageOperation.Status.DELIVERED) {
                logger.debug("Forwarded successfully - deleting message from local storage");
                localNode.getMessages().getForwardMessages().remove(entry.getKey());

                TextMessage msg = smo.getMessage();

                // send MESSAGE SUCCESS ACK here
                NotifySuccessOperation nso = new NotifySuccessOperation(localNode.getServer(), localNode, msg.getOrigin(), msg.getRecipientUser().getUserName(), msg.getMessageId(), localNode.getConfig());
                nso.execute();
            }
        } catch (IOException e) {
            logger.error("Message refresh failed with error:", e);
        }
    }
}
