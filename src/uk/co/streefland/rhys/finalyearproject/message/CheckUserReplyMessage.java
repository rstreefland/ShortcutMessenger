package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Responds to a FindNodeMessage with a list of the K closest nodes to the provided KeyId
 */
public class CheckUserReplyMessage implements Message {

    private Node origin;
    private User existingUser;

    public static final byte CODE = 0x08;

    public CheckUserReplyMessage(Node origin, User existingUser) {
        this.origin = origin;
        this.existingUser = existingUser;
    }

    public CheckUserReplyMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        /* Add the origin node to the stream */
        origin.toStream(out);

        /* Add the existingUser to the stream */
        existingUser.toStream(out);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        /* Read in the origin */
        origin = new Node(in);

        /* Read the existing user from the stream */
        existingUser = new User(in);
    }

    @Override
    public String toString() {
        return "CheckUserReplyMessage[origin KeyId=" + origin.getNodeId() + "]";
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
