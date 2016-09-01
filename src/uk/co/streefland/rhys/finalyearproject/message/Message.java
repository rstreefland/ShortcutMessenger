package uk.co.streefland.rhys.finalyearproject.message;

/**
 * Interface for every message to implement
 */
public interface Message extends Streamable {

    /* Identifies the type of the message */
    byte code();
}
