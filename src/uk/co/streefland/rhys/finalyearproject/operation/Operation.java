package uk.co.streefland.rhys.finalyearproject.operation;

import java.io.IOException;

public interface Operation {

    /**
     * Starts an operation and returns when the operation is finished
     *
     * @throws
     */
    public void execute() throws IOException;
}
