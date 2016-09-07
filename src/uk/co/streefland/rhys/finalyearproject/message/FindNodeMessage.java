package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Provides a KeyId to a remote node and requests the K closest nodes to that KeyId in return
 */
public class FindNodeMessage implements Message {

    private Node origin;
    private KeyId lookupId;

    public static final byte CODE = 0x03;

    public FindNodeMessage(Node origin, KeyId lookup) {
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
        lookupId = new KeyId(in);
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

    public KeyId getLookupId() {
        return lookupId;
    }
}
