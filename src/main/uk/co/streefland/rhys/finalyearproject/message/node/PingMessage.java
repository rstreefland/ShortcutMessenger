package uk.co.streefland.rhys.finalyearproject.message.node;

import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Message sent to a node during the initial bootstrap
 */
public class PingMessage implements Message {

    public static final byte CODE = 0x09;
    private Node origin;

    public PingMessage(Node origin) {
        this.origin = origin;
    }

    public PingMessage(DataInputStream in) throws IOException {
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
        return "PingMessage[origin KeyId=" + origin.getNodeId() + "]";
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    public Node getOrigin() {
        return origin;
    }
}