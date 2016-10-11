package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Acknowledgement message sent in reply to various messages
 */
public class AcknowledgeMessage implements Message {

    public static final byte CODE = 0x01;
    private Node origin;
    private boolean operationSuccessful;

    public AcknowledgeMessage(Node origin, boolean operationSuccessful) {
        this.origin = origin;
        this.operationSuccessful = operationSuccessful;
    }

    public AcknowledgeMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);
        out.writeBoolean(operationSuccessful);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        origin = new Node(in);
        operationSuccessful = in.readBoolean();
    }

    @Override
    public String toString() {
        return "AcknowledgeMessage[origin=" + origin.getNodeId() + "]";
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    public Node getOrigin() {
        return origin;
    }

    public boolean getOperationSuccessful() {
        return operationSuccessful;
    }
}
