package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.*;

/**
 * Created by Rhys on 03/09/2016.
 */
public class TextMessage implements Message {

    Node origin;
    String message;
    public static final byte CODE = 0x03;

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

    public String getMessage() {
        return message;
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
        return "TextMessage[origin NodeId=" + origin.getNodeId() + "]";
    }
}
