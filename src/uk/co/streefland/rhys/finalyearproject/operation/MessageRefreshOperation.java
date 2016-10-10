package uk.co.streefland.rhys.finalyearproject.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.message.TextMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;

import java.io.IOException;
import java.util.Map;

/**
 * Refreshes all buckets within the RoutingTable
 */
public class MessageRefreshOperation implements Operation {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Server server;
    private final LocalNode localNode;
    private final Configuration config;

    public MessageRefreshOperation(Server server, LocalNode localNode, Configuration config) {
        this.server = server;
        this.localNode = localNode;
        this.config = config;
    }

    /**
     * Refreshes each bucket in a separate thread using the FindNodeOperation.
     *
     * @throws IOException
     */
    @Override
    public synchronized void execute() throws IOException {

        Map<KeyId, TextMessage> forwardMessages = localNode.getMessages().getForwardMessages();

        System.out.println("Forward messages size: " +  localNode.getMessages().getForwardMessages().size());
        System.out.println("Forward messages size: " +  forwardMessages);

        for (Map.Entry<KeyId, TextMessage> entry : forwardMessages.entrySet()) {
            /* Run each SendMessageOperation in a different thread */
            new Thread() {
                @Override
                public void run() {
                    try {
                        SendMessageOperation smo = new SendMessageOperation(server, localNode, config, entry.getValue().getTargetUser(), entry.getValue(), true);
                        smo.execute();
                        if (smo.isMessagedSuccessfully()) {
                            logger.info("Forwarded successfully - deleting message from local storage");
                            forwardMessages.remove(entry.getKey());
                        }
                    } catch (IOException e) {
                        logger.error("Message refresh failed with error:", e);
                    }
                }
            }.start();
        }
    }
}
