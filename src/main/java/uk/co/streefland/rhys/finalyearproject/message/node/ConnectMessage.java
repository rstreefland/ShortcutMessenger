package uk.co.streefland.rhys.finalyearproject.message.node;

import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Message sent to a node during the initial bootstrap
 */
public class ConnectMessage implements Message {

    public static final byte CODE = 0x02;
    private Node origin;

    public ConnectMessage(Node origin) {
        this.origin = origin;
    }

    public ConnectMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        origin = new Node(in);
    }

    @Override
    public String toString() {
        return "ConnectMessage[origin KeyId=" + origin.getNodeId() + "]";
    }

    @Override
    public KeyId getNetworkId() {
        return null;
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
}