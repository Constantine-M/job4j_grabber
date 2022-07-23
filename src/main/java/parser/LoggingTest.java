package parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Constantine on 16.07.2022
 */
public class LoggingTest {

    private final static Logger LOG = LoggerFactory.getLogger(LoggingTest.class);

    public static void testFileLog() {
        try {
            throw new Exception("There is no code here");
        } catch (Exception e) {
            LOG.error("Exception write to file", e);
        }
    }

    public static void main(String[] args) {
        testFileLog();
        LOG.info("Info message..");
        LOG.warn("Warn message..");
        LOG.debug("Debug message..");
        LOG.error("Error message..");
    }
}
