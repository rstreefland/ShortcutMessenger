package uk.co.streefland.rhys.finalyearproject.message.node;

import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Provides a KeyId to a remote node and requests the K closest nodes to that KeyId in return
 */
public class FindNodeMessage implements Message {

    public static final byte CODE = 0x03;
    private KeyId networkId;
    private Node origin;
    private KeyId lookupId;

    public FindNodeMessage(KeyId networkId, Node origin, KeyId lookup) {
        this.networkId = networkId;
        this.origin = origin;
        this.lookupId = lookup;
    }

    public FindNodeMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        networkId.toStream(out);
        origin.toStream(out);
        lookupId.toStream(out);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        networkId = new KeyId(in);
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

    public KeyId getLookupId() {
        return lookupId;
    }
}
