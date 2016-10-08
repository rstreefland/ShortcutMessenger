package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * A simple text message - slowly getting more functional :)
 */
public class TextMessage implements Message {

    private Node origin;
    private Node target;
    private User originUser;
    private KeyId messageId;
    private String message;
    private long createdTime;

    public static final byte CODE = 0x05;

    public TextMessage(Node originNode, Node target, User originUser, String message) {
        this.origin = originNode;
        this.target = target;
        this.originUser = originUser;
        this.message = message;
        this.messageId = new KeyId();

        this.createdTime = new Date().getTime() / 1000; // store timestamp in seconds
    }

    public TextMessage(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException {
        origin = new Node(in);

        if (in.readBoolean()) {
            target = new Node(in);
        }

        message = in.readUTF();

        messageId = new KeyId(in);

        if (in.readBoolean()) {
            originUser = new User(in);
        }
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);

        if (target != null) {
            out.writeBoolean(true);
            target.toStream(out);
        } else {
            out.writeBoolean(false);
        }

        out.writeUTF(message);

        messageId.toStream(out);

        if (originUser != null) {
            out.writeBoolean(true);
            originUser.toStream(out);
        } else {
            out.writeBoolean(false);
        }
    }

    @Override
    public String toString() {
        return "TextMessage[origin KeyId=" + origin.getNodeId() + "]";
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    public Node getOrigin() {
        return origin;
    }

    public Node getTarget() {
        return target;
    }

    public User getOriginUser() {
        return originUser;
    }

    public KeyId getMessageId() {
        return messageId;
    }

    public String getMessage() {
        return message;
    }

    public long getCreatedTime() {
        return createdTime;
    }
}
