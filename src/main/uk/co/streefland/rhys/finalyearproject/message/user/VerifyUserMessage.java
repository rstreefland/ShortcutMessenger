package uk.co.streefland.rhys.finalyearproject.message.user;

import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Sends a user object for verification as part of the login operation
 */
public class VerifyUserMessage implements Message {

    public static final byte CODE = 0x06;
    private Node origin;
    private User user;

    public VerifyUserMessage(Node origin, User user) {
        this.origin = origin;
        this.user = user;
    }

    public VerifyUserMessage(DataInputStream in) throws IOException {
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
        return "VerifyUserMessage[origin KeyId=" + origin.getNodeId() + "]";
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
