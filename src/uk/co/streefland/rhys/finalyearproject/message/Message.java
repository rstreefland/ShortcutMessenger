package uk.co.streefland.rhys.finalyearproject.message;

/**
 * Interface that every message should implement to ensure consistency
 */
public interface Message extends Streamable {

    /* Identifies the type of the message */
    byte code();
}
