package uk.co.streefland.rhys.finalyearproject.operation.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.message.content.TextMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;
import uk.co.streefland.rhys.finalyearproject.operation.SendMessageOperation;

import java.io.IOException;
import java.util.Map;

/**
 * Forwards messages to their intended recipients
 */
public class MessageRefreshOperation implements Operation {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalNode localNode;

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

        /* Remove all messages older than two days */
        localNode.getMessages().cleanUp();

        for (Map.Entry<KeyId, TextMessage> entry : localNode.getMessages().getForwardMessages().entrySet()) {
            /* Run each SendMessageOperation in a different thread */
            new Thread() {
                @Override
                public void run() {
                    try {
                        SendMessageOperation smo = new SendMessageOperation(localNode, entry.getValue().getRecipientUser(), entry.getValue());
                        smo.execute();
                        if (smo.isMessagedSuccessfully()) {
                            logger.info("Forwarded successfully - deleting message from local storage");
                            localNode.getMessages().getForwardMessages().remove(entry.getKey());
                        }
                    } catch (IOException e) {
                        logger.error("Message refresh failed with error:", e);
                    }
                }
            }.start();
        }
    }
}
