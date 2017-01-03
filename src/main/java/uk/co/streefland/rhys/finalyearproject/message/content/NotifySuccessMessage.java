package uk.co.streefland.rhys.finalyearproject.message.content;

import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Success message sent by forwarding node to indicate that the message has been forwarded successfully
 */
public class NotifySuccessMessage implements Message {

    public static final byte CODE = 0x09;
    private KeyId networkId;
    private Node origin;
    private String recipient;
    private KeyId messageId;

    public NotifySuccessMessage(KeyId networkId, Node origin, String recipient, KeyId messageId) {
        this.networkId = networkId;
        this.origin = origin;
        this.recipient = recipient;
        this.messageId = messageId;
    }

    public NotifySuccessMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        networkId.toStream(out);
        origin.toStream(out);
        out.writeUTF(recipient);
        messageId.toStream(out);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        networkId = new KeyId(in);
        origin = new Node(in);
        recipient = in.readUTF();
        messageId = new KeyId(in);
    }

    @Override
    public KeyId getNetworkId() {
        return networkId;
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public Node getOrigin() {
        return origin;
    }

    @Override
    public Node getSource() {
        return origin;
    }

    public String getRecipient() {
        return recipient;
    }

    public KeyId getMessageId() {
        return messageId;
    }

    @Override
    public String toString() {
        return "NotifySuccessMessage[origin=" + origin.getNodeId() + "]";
    }
}
