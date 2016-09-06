package uk.co.streefland.rhys.finalyearproject.message;

import uk.co.streefland.rhys.finalyearproject.node.Node;

/**
 * Interface that every message should implement to ensure consistency
 */
public interface Message extends Streamable {

    @Override
    String toString();

    byte getCode();

    Node getOrigin();
}
