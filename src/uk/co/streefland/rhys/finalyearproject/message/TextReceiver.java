package uk.co.streefland.rhys.finalyearproject.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Encryption;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Receives a TextMessage and utilises the Messages class to handle the message
 */
public class TextReceiver implements Receiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LocalNode localNode;
    private final Server server;

    public TextReceiver(LocalNode localNode) {
        this.localNode = localNode;
        this.server = localNode.getServer();
    }

    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        TextMessage msg = (TextMessage) incoming;

        /* If target is null - it's a broadcast message */
        if (msg.getTarget() != null) {
            /* If the message is intended for this node */
            if (msg.getTarget().getNodeId().equals(localNode.getNode().getNodeId())) {
                if (msg.getOriginUser() != null) {
                    logger.info("Received a message intended for me");
                    if (msg.getMessage() == null) {
                        logger.info("Is encrypted: TRUE");
                    }

                    try {
                        Encryption enc = new Encryption();
                        String message = enc.decryptString(msg.getTargetUser(), localNode.getUsers().getLocalUser(), msg.getIv(), msg.getEncryptedMessage());
                        msg.setMessage(message);
                    } catch (InvalidAlgorithmParameterException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    }

                    localNode.getMessages().addReceivedMessage(msg);
                }
            } else {
                /* This is a message intended for a different target - handle */
                logger.info("Received a message intended for another node");
                localNode.getMessages().addForwardMessage(msg);
            }
        } else {
            System.out.println("Broadcast message received: " + msg.getMessage());
        }

        /* Create the AcknowledgeMessage */
        Message ack = new AcknowledgeMessage(localNode.getNode(), true);

        /* The server sends the reply */
        if (server.isRunning()) {
            server.reply(msg.getOrigin(), ack, communicationId);
        }
    }

    /**
     * Nothing to be done here
     *
     * @param communicationId
     * @throws IOException
     */
    @Override
    public void timeout(int communicationId) throws IOException {
    }
}
