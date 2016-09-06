package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.*;

/**
 * A simple broadcast message - for testing purposes only
 */
public class TextMessage implements Message {

    Node origin;
    String message;

    public static final byte CODE = 0x05;

    public TextMessage(Node origin, String message) {
        this.origin = origin;
        this.message = message;
    }

    public TextMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        origin = new Node(in);
        message = in.readUTF();
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);
        out.writeUTF(message);
    }

    @Override
    public String toString() {
        return "TextMessage[origin NodeId=" + origin.getNodeId() + "]";
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    public Node getOrigin() {
        return origin;
    }

    public String getMessage() {
        return message;
    }
}
