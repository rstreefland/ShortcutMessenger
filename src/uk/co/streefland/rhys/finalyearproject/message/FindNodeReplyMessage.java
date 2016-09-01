package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Responds to a FindNodeMessage with a list of the K closest nodes to the provided NodeId
 */
public class FindNodeReplyMessage implements Message {

    private Node origin;
    public static final byte CODE = 0x06;
    private List<Node> nodes;

    public FindNodeReplyMessage(Node origin, List<Node> nodes) {
        this.origin = origin;
        this.nodes = nodes;
    }

    public FindNodeReplyMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        /* Read in the origin */
        this.origin = new Node(in);

        /* Get the number of incoming nodes */
        int len = in.readInt();
        this.nodes = new ArrayList<>(len);

        /* Read in all nodes */
        for (int i = 0; i < len; i++) {
            this.nodes.add(new Node(in));
        }
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        /* Add the origin node to the stream */
        origin.toStream(out);

        /* Write the number of nodes and the nodes to the stream */
        out.writeInt(this.nodes.size());
        for (Node node : this.nodes) {
            node.toStream(out);
        }
    }

    public List<Node> getNodes() {
        return this.nodes;
    }

    public Node getOrigin() {
        return this.origin;
    }

    @Override
    public byte code() {
        return CODE;
    }

    @Override
    public String toString() {
        return "FindNodeReplyMessage[origin NodeId=" + origin.getNodeId() + "]";
    }
}
