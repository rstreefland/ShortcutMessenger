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

    private Node origin;
    private User user;
    private boolean verify; // should we verify it matches or just check it exists

    public static final byte CODE = 0x07;

    public VerifyUserMessage(Node origin, User user, boolean verify) {
        this.origin = origin;
        this.user = user;
        this.verify = verify;
    }

    public VerifyUserMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        origin = new Node(in);
        user = new User(in);
        verify = in.readBoolean();
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);
        user.toStream(out);
        out.writeBoolean(verify);
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

    public boolean isVerify() {
        return verify;
    }
}
