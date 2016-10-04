package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This message is sent during the register user operation and contains the user object that the remote node should store
 */
public class StoreUserMessage implements Message {

    Node origin;
    User user;

    public static final byte CODE = 0x06;

    public StoreUserMessage(Node origin, User user) {
        this.origin = origin;
        this.user = user;
    }

    public StoreUserMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        origin = new Node(in);
        user = new User(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
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

    public Node getOrigin() {
        return origin;
    }

    public User getUser() {
        return user;
    }
}
