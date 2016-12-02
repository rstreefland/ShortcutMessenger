package uk.co.streefland.rhys.finalyearproject.message.user;

import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Response to a VerifyUserMessage which contains the existing user object if it exists on the remote node
 */
public class VerifyUserMessageReply implements Message {

    public static final byte CODE = 0x07;
    private KeyId networkId;
    private Node origin;
    private User existingUser;

    public VerifyUserMessageReply(KeyId networkId, Node origin, User existingUser) {
        this.networkId = networkId;
        this.origin = origin;
        this.existingUser = existingUser;
    }

    public VerifyUserMessageReply(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        networkId.toStream(out);

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
        networkId = new KeyId(in);

        /* Read in the origin */
        origin = new Node(in);

        /* Read the existing user from the stream if not null */
        if (in.readBoolean()) {
            existingUser = new User(in);
        }
    }

    @Override
    public String toString() {
        return "VerifyUserMessageReply[origin KeyId=" + origin.getNodeId() + "]";
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public KeyId getNetworkId() {
        return networkId;
    }

    public Node getOrigin() {
        return origin;
    }

    @Override
    public Node getSource() {
        return origin;
    }

    public User getExistingUser() {
        return existingUser;
    }
}
