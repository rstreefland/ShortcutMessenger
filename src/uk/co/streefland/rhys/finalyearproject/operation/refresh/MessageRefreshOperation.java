package uk.co.streefland.rhys.finalyearproject.operation.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.message.TextMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.operation.Operation;
import uk.co.streefland.rhys.finalyearproject.operation.SendMessageOperation;

import java.io.IOException;
import java.util.Map;

/**
 * Refreshes all buckets within the RoutingTable
 */
public class MessageRefreshOperation implements Operation {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LocalNode localNode;

    public MessageRefreshOperation(LocalNode localNode) {
        this.localNode = localNode;
    }

    /**
     * Refreshes each bucket in a separate thread using the FindNodeOperation.
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {

        for (Map.Entry<KeyId, TextMessage> entry : localNode.getMessages().getForwardMessages().entrySet()) {
            /* Run each SendMessageOperation in a different thread */
            new Thread() {
                @Override
                public void run() {
                    try {
                        SendMessageOperation smo = new SendMessageOperation(localNode, entry.getValue().getTargetUser(), entry.getValue(), true);
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
