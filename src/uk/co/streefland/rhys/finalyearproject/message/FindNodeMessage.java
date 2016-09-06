package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.node.NodeId;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Provides a NodeId to a remote node and requests the K closest nodes to that NodeId in return
 */
public class FindNodeMessage implements Message {

    private Node origin;
    private NodeId lookupId;

    public static final byte CODE = 0x03;

    public FindNodeMessage(Node origin, NodeId lookup) {
        this.origin = origin;
        this.lookupId = lookup;
    }

    public FindNodeMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);
        lookupId.toStream(out);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        origin = new Node(in);
        lookupId = new NodeId(in);
    }

    @Override
    public String toString() {
        return "FindNodeMessage[origin=" + origin + ",lookup=" + lookupId + "]";
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    public Node getOrigin() {
        return origin;
    }

    public NodeId getLookupId() {
        return lookupId;
    }
}
