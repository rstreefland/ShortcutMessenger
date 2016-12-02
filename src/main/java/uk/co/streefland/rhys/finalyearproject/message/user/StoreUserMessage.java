package uk.co.streefland.rhys.finalyearproject.message.user;

import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This message is sent during the register user operation and contains the user object that the remote node should store
 */
public class StoreUserMessage implements Message {

    public static final byte CODE = 0x05;
    private KeyId networkId;
    private Node origin;
    private User user;

    public StoreUserMessage(KeyId networkId, Node origin, User user) {
        this.networkId = networkId;
        this.origin = origin;
        this.user = user;
    }

    public StoreUserMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        networkId = new KeyId(in);
        origin = new Node(in);
        user = new User(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        networkId.toStream(out);
        origin.toStream(out);
        user.toStream(out);
    }

    @Override
    public String toString() {
        return "StoreUserMessage[origin KeyId=" + origin.getNodeId() + "]";
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

    public User getUser() {
        return user;
    }
}
