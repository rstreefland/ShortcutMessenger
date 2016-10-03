package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Interface that every message should implement to ensure consistency
 */
public interface Message {

    void toStream(DataOutputStream out) throws IOException;

    void fromStream(DataInputStream out) throws IOException;

    @Override
    String toString();

    byte getCode();

    Node getOrigin();
}
