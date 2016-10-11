package uk.co.streefland.rhys.finalyearproject.message.node;

import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Response to a FindNodeMessage with a list of the K closest nodes to the provided KeyId
 */
public class FindNodeReplyMessage implements Message {

    private Node origin;
    private List<Node> nodes;

    public static final byte CODE = 0x04;

    public FindNodeReplyMessage(Node origin, List<Node> nodes) {
        this.origin = origin;
        this.nodes = nodes;
    }

    public FindNodeReplyMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        /* Add the origin node to the stream */
        origin.toStream(out);

        /* Write the number of nodes and the nodes to the stream */
        out.writeInt(nodes.size());
        for (Node node : nodes) {
            node.toStream(out);
        }
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        /* Read in the origin */
        origin = new Node(in);

        /* Get the number of incoming nodes */
        int len = in.readInt();
        nodes = new ArrayList<>(len);

        /* Read in all nodes */
        for (int i = 0; i < len; i++) {
            nodes.add(new Node(in));
        }
    }

    @Override
    public String toString() {
        return "FindNodeReplyMessage[origin KeyId=" + origin.getNodeId() + "]";
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    public Node getOrigin() {
        return origin;
    }

    public List<Node> getNodes() {
        return nodes;
    }
}
