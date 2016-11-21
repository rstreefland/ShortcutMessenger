package uk.co.streefland.rhys.finalyearproject.operation;

import java.io.IOException;

/**
 * Interface that every operation should implement to ensure consistency
 */
public interface Operation {

    /**
     * Starts an operation and returns when the operation is finished
     */
    void execute() throws IOException;
}
