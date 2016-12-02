package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Interface that every message should implement
 */
public interface Message {

    /**
     * Writes the message data to the output stream
     *
     * @param out
     * @throws IOException
     */
    void toStream(DataOutputStream out) throws IOException;

    /**
     * Reads the message data from the output stream
     *
     * @param out
     * @throws IOException
     */
    void fromStream(DataInputStream out) throws IOException;

    @Override
    String toString();

    byte getCode();

    KeyId getNetworkId();

    Node getOrigin();

    Node getSource();
}
