package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Response to a VerifyUserMessage which contains the existing user object if it exists on the remote node
 */
public class VerifyUserReplyMessage implements Message {

    private Node origin;
    private User existingUser;

    public static final byte CODE = 0x08;

    public VerifyUserReplyMessage(Node origin, User existingUser) {
        this.origin = origin;
        this.existingUser = existingUser;
    }

    public VerifyUserReplyMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        /* Add the origin node to the stream */
        origin.toStream(out);

        /* Add the existingUser to the stream if not null */
        if (existingUser != null) {
            out.writeBoolean(true);
            existingUser.toStream(out);
        } else {
            out.writeBoolean(false);
        }
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        /* Read in the origin */
        origin = new Node(in);

        /* Read the existing user from the stream if not null */
        if (in.readBoolean()) {
            existingUser = new User(in);
        }
    }

    @Override
    public String toString() {
        return "VerifyUserReplyMessage[origin KeyId=" + origin.getNodeId() + "]";
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    public Node getOrigin() {
        return origin;
    }

    public User getExistingUser() {
        return existingUser;
    }
}
