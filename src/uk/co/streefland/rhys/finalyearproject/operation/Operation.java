package uk.co.streefland.rhys.finalyearproject.operation;

import java.io.IOException;

public interface Operation {

    /**
     * Starts an operation and returns when the operation is finished
     *
     * @throws
     */
    void execute() throws IOException;
}
