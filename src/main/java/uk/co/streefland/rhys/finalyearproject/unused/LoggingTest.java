package uk.co.streefland.rhys.finalyearproject.unused;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test SLF4J
 */
class LoggingTest {
    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(LoggingTest.class);

        logger.debug("Hello World");

        logger.info("Hello World");

        logger.warn("Hello World");

        logger.error("Hello World");
    }
}
