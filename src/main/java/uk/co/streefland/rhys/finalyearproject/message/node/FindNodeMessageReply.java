package uk.co.streefland.rhys.finalyearproject.message.node;

import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Response to a FindNodeMessage with a list of the K closest nodes to the provided KeyId
 */
public class FindNodeMessageReply implements Message {

    public static final byte CODE = 0x04;
    private KeyId networkId;
    private Node origin;
    private List<Node> nodes;

    public FindNodeMessageReply(KeyId networkId, Node origin, List<Node> nodes) {
        this.networkId = networkId;
        this.origin = origin;
        this.nodes = nodes;
    }

    public FindNodeMessageReply(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        networkId.toStream(out);

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
        networkId = new KeyId(in);

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
        return "FindNodeMessageReply[origin KeyId=" + origin.getNodeId() + "]";
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

    public List<Node> getNodes() {
        return nodes;
    }
}
