package uk.co.streefland.rhys.finalyearproject.exceptions;

import java.io.IOException;

/**
 * Thrown when the localNode is unable to send a message to a remote node
 */
public class BootstrapException extends IOException {

    public BootstrapException() {
        super();
    }

    public BootstrapException(String message) {
        super(message);
    }

}
