package uk.co.streefland.rhys.finalyearproject.message.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Encryption;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Server;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.message.AcknowledgeMessage;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.operation.NotifySuccessOperation;
import uk.co.streefland.rhys.finalyearproject.operation.SendMessageOperation;

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
        User localUser = localNode.getUsers().getLocalUser();

        /* If there is a user logged in */
        if (localUser != null) {
            /* If the user currently logged in matches the target user of the message*/
            if (localUser.getUserId().equals(msg.getRecipientUser().getUserId())) {
                logger.info("Received a message intended for me");
                try {
                    /* Decrypt the message */
                    Encryption enc = new Encryption();
                    String message = enc.decryptString(msg.getRecipientUser(), localNode.getUsers().getLocalUserPassword(), msg.getIv(), msg.getEncryptedMessage());
                    msg.setMessage(message);

                    /* Store the message */
                    localNode.getMessages().addReceivedMessage(msg);
                } catch (InvalidAlgorithmParameterException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException e) {
                    logger.error("Failed to decrypt message", e);
                }

                Message ack = new AcknowledgeMessage(localNode.getNetworkId(), localNode.getNode(), true);

                /* The server sends the reply */
                if (server.isRunning()) {
                    server.reply(msg.getSource(), ack, communicationId);
                }
            } else {
                forwardMessage(msg, communicationId);
            }
        } else {
            forwardMessage(msg, communicationId);
        }
    }

    private void forwardMessage(TextMessage msg, int communicationId) throws IOException {
        /* Create the AcknowledgeMessage
           This is done here so the ack doesn't time out on the origin node
           And because the origin node gets overwritten by the SendMessageOperation */
        Message ack = new AcknowledgeMessage(localNode.getNetworkId(), localNode.getNode(), true);

        /* The server sends the reply */
        if (server.isRunning()) {
            server.reply(msg.getSource(), ack, communicationId);
        }

        /* This is a message intended for a different target - handle */
        logger.info("Received a message intended for another node");

        SendMessageOperation smo = new SendMessageOperation(localNode, msg.getRecipientUser(), msg);
        smo.execute();

        if (smo.messageStatus() == SendMessageOperation.Status.DELIVERED) {
            logger.info("Message forwarded successfully - no need to add it to forward messages");

            NotifySuccessOperation nso = new NotifySuccessOperation(localNode, msg.getOrigin(), msg.getRecipientUser().getUserName(), msg.getMessageId());
            nso.execute();

            logger.info("Message success message sent!");

        } else {
            logger.info("Couldn't forward immediately - adding to forward messages");
            localNode.getMessages().addForwardMessage(msg);
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
