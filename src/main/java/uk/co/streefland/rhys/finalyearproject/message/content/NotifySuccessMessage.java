package uk.co.streefland.rhys.finalyearproject.message.content;

import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Acknowledgement message sent in reply to various messages
 */
public class NotifySuccessMessage implements Message {

    public static final byte CODE = 0x09;
    private KeyId networkId;
    private Node origin;
    private KeyId messageId;

    public NotifySuccessMessage(KeyId networkId, Node origin, KeyId messageId) {
        this.networkId = networkId;
        this.origin = origin;
        this.messageId = messageId;
    }

    public NotifySuccessMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        networkId.toStream(out);
        origin.toStream(out);
        messageId.toStream(out);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        networkId = new KeyId(in);
        origin = new Node(in);
        messageId = new KeyId(in);
    }

    @Override
    public String toString() {
        return "NotifySuccessMessage[origin=" + origin.getNodeId() + "]";
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public KeyId getNetworkId() {
        return networkId;
    }

    @Override
    public Node getOrigin() {
        return origin;
    }

    @Override
    public Node getSource() {
        return origin;
    }

    public KeyId getMessageId() {
        return messageId;
    }
}
