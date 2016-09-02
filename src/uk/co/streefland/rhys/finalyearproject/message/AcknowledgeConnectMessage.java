package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Acknowledgement message sent in reply to an incoming ConnectMessage by the ConnectReceiver
 */
public class AcknowledgeConnectMessage implements Message {

    private Node origin;
    public static final byte CODE = 0x01;

    public AcknowledgeConnectMessage(Node origin) {
        this.origin = origin;
    }

    public AcknowledgeConnectMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        origin = new Node(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);
    }

    public Node getOrigin() {
        return origin;
    }

    @Override
    public byte code() {
        return CODE;
    }

    @Override
    public String toString() {
        return "AcknowledgeConnectMessage[origin=" + origin.getNodeId() + "]";
    }
}
